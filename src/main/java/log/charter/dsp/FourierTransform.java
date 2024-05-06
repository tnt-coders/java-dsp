package log.charter.dsp;

import static org.bytedeco.fftw.global.fftw3.*;

import java.util.Arrays;

import org.bytedeco.javacpp.*;

public class FourierTransform {
	private static final int REAL = 0;
	private static final int IMAG = 1;
	
	private final int bufferSize;
	private FloatPointer input;
	private FloatPointer output;
	private fftwf_plan plan;
	
	// Temporary buffer used to translate the output into Complex values
	private float[] outputBuffer;
	
	// Performs FFT of real input data
	public FourierTransform(final int bufferSize) {
		this.bufferSize = bufferSize;
		input = fftwf_alloc_real(bufferSize);
		output = fftwf_alloc_complex(bufferSize / 2 + 1);
		outputBuffer = new float[(bufferSize / 2 + 1) * 2];
		plan = fftwf_plan_dft_r2c_1d(bufferSize, input, output, FFTW_ESTIMATE);
	}
	
	public float[] allocInput() {
		return new float[bufferSize];
	}
	
	public Complex[] allocOutput() {
		Complex[] output = new Complex[bufferSize / 2 + 1];
		Arrays.fill(output, new Complex());
		return output;
	}
	
	public void execute(final float[] input, Complex[] output) {
		this.input.put(input);
		fftwf_execute(this.plan);
		this.output.get(outputBuffer);
		
		// Normalize the output and store it in the output array
		for (int bin = 0; bin < output.length; ++bin) {
			output[bin].real = outputBuffer[2 * bin + REAL] / bufferSize;
			output[bin].imag = outputBuffer[2 * bin + IMAG] / bufferSize;
			
			// Prints correct output
			System.out.println(output[bin].real + ", " + output[bin].imag);
		}
		
		for (int bin = 0; bin < output.length; ++bin) {
			
			// Prints incorrect output
			System.out.println(output[bin].real + ", " + output[bin].imag);
    	}
	}
	
	// Cleanup
	@Override
	protected void finalize() {
		fftwf_destroy_plan(this.plan);
		fftwf_free(this.input);
		fftwf_free(this.output);
	}
}
