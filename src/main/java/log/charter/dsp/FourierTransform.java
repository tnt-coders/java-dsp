package log.charter.dsp;

import static org.bytedeco.fftw.global.fftw3.*;

import org.bytedeco.javacpp.*;

public class FourierTransform {
	private final int bufferSize;
	private FloatPointer input;
	private FloatPointer output;
	private fftwf_plan plan;
	
	// Performs FFT of real input data
	public FourierTransform(final int bufferSize) {
		this.bufferSize = bufferSize;
		this.input = fftwf_alloc_real(this.bufferSize);
		this.output = fftwf_alloc_complex(this.bufferSize / 2 + 1);
		this.plan = fftwf_plan_dft_r2c_1d(this.bufferSize, this.input, this.output, FFTW_ESTIMATE);
	}
	
	public int inputSize() {
		return this.bufferSize;
	}
	
	public int outputSize() {
		return (this.bufferSize / 2 + 1) * 2;
	}
	
	public void execute(final float[] input, float[] output) {
		this.input.put(input);
		fftwf_execute(this.plan);
		this.output.get(output);
	}
	
	// Cleanup
	@Override
	protected void finalize() {
		fftwf_destroy_plan(this.plan);
		fftwf_free(this.input);
		fftwf_free(this.output);
	}
}
