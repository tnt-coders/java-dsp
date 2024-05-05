package log.charter.dsp;

import java.util.Arrays;

public class NoteExtractor {
	private static final int REAL = 0;
	private static final int IMAG = 1;
	
	// Declare reference frequency of A4
	private static final float A4 = 440f;
	
	// Go down as low as C0 (57 semitones below A4)
	private static final float SEMITONES_BELOW_A4 = 57;
	
	// Span 8 octaves
	private static final int OCTAVES = 8;
	private static final int NOTES_PER_OCTAVE = 12;
	private static final int NOTE_COUNT = OCTAVES * NOTES_PER_OCTAVE;
	
	// 1/4 tone resolution
	private static final float RESOLUTION = 0.25f;
	private static final int BIN_COUNT = (int)(NOTE_COUNT * (0.5f / RESOLUTION));
	private static final int BINS_PER_OCTAVE = (int)(NOTES_PER_OCTAVE * (0.5f / RESOLUTION));
	
	private final float[] frequencyBins = new float[BIN_COUNT];
	
	private final FourierTransform fft;
	private final float[] window;
	private final float[] fftInput;
	private final float[] fftOutput;
	
	NoteExtractor(final int bufferSize, final int sampleRate) { 
		this(bufferSize, sampleRate, 0);
	}
	
	NoteExtractor(final int bufferSize, final int sampleRate, final int centOffset) {
		if (bufferSize > sampleRate) {
			throw new IllegalArgumentException("NoteExtractor input buffer must be less than 1 second in length.");
		}
		
		// Calculate the frequency bins
		final float ref_frequency = A4 * (float)Math.pow(2, centOffset / (NOTES_PER_OCTAVE * 1000f));
		for (int bin = 0; bin < this.frequencyBins.length; ++bin) {
			frequencyBins[bin] = ref_frequency * (float)Math.pow(2, (bin - SEMITONES_BELOW_A4 * (0.5f / RESOLUTION)) / (float)BINS_PER_OCTAVE);
		}
		
		// Initialize the FFT with a buffer size equal to the sample rate
		// This makes the outputs precisely 1Hz apart and simplifies calculations
		fft = new FourierTransform(sampleRate);
		
		// Create a window
		window = HammingWindow.generate(bufferSize);
		
		// Initialize the FFT input (zero pad up to the sample rate)
		fftInput = new float[fft.inputSize()];
		Arrays.fill(fftInput, 0f);
		
		fftOutput = new float[fft.outputSize()];
	}
	
	public float[] frequencies() {
		return this.frequencyBins;
	}
	
	public Complex[] allocOutput() {
		Complex[] output = new Complex[BIN_COUNT];
		Arrays.fill(output, new Complex());
		return output;
	}
	
	public void execute(float[] input, Complex[] output) {
		if (input.length != window.length) {
			throw new IllegalArgumentException("NoteExtractor input size does not match the specified buffer size.");
		}
		
		// Copy the input signal into the FFT input buffer
		// The buffer is already appropriately zero padded from when it was initialized
		for (int i = 0; i < input.length; ++i) {
			fftInput[i] = input[i] * window[i];
		}

		fft.execute(fftInput, fftOutput);
		
		// Normalize the output
		for (int i = 0; i < fftOutput.length; ++i) {
			fftOutput[i] /= fftInput.length;
		}
		
		// Interpolate the results to line up with musical notes
		for (int bin = 0; bin < frequencyBins.length; ++bin) {
			final float frequency = frequencyBins[bin];
			
			// Because FFT outputs line up with their corresponding frequencies
			// fLow and fHigh can be used directly to index fftOutput
			final int fLow = (int)Math.floor(frequency);
			final int fHigh = fLow + 1;
			
			// Multiply all indexes by 2 because the output is complex and takes 2 indexes per number
			output[bin].real = fftOutput[2 * fLow + REAL] + (frequency - fLow) * (fftOutput[2 * fHigh + REAL] - fftOutput[2 * fLow + REAL]);
			output[bin].imag = fftOutput[2 * fLow + IMAG] + (frequency - fLow) * (fftOutput[2 * fHigh + IMAG] - fftOutput[2 * fLow + IMAG]);
		}
	}
}
