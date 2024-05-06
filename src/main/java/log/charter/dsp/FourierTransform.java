package log.charter.dsp;

import static org.bytedeco.fftw.global.fftw3.*;

import java.util.Arrays;

import org.bytedeco.javacpp.*;

public class FourierTransform {
	private static final int REAL = 0;
	private static final int IMAG = 1;
	
	private final int bufferSize;
	private FloatPointer fftInput;
	private FloatPointer fftOutput;
	private fftwf_plan fftPlan;
	
	// Temporary buffer used to translate the output into Complex values
	private float[] outputBuffer;
	
	// Performs FFT of real input data
	public FourierTransform(final int bufferSize) {
		this.bufferSize = bufferSize;
		fftInput = fftwf_alloc_real(bufferSize);
		fftOutput = fftwf_alloc_complex(bufferSize / 2 + 1);
		fftPlan = fftwf_plan_dft_r2c_1d(bufferSize, fftInput, fftOutput, FFTW_ESTIMATE);
		
		outputBuffer = new float[(bufferSize / 2 + 1) * 2];
	}
	
	public float[] allocInput() {
		return new float[bufferSize];
	}
	
	public Complex[] allocOutput() {
		Complex[] output = new Complex[bufferSize / 2 + 1];
		for (int bin = 0; bin < output.length; ++bin) {
			output[bin] = new Complex();
		}
		
		return output;
	}
	
	public void execute(final float[] input, Complex[] output) {
		this.fftInput.put(input);
		fftwf_execute(this.fftPlan);
		this.fftOutput.get(outputBuffer);
		
		// Normalize the output and store it in the output array
		for (int bin = 0; bin < output.length; ++bin) {
			output[bin].real = outputBuffer[2 * bin + REAL] / bufferSize;
			output[bin].imag = outputBuffer[2 * bin + IMAG] / bufferSize;
		}
	}
	
	// Cleanup
	@Override
	protected void finalize() {
		fftwf_destroy_plan(this.fftPlan);
		fftwf_free(this.fftInput);
		fftwf_free(this.fftOutput);
	}
}
