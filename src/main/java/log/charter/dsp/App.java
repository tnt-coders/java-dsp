package log.charter.dsp;

import static java.lang.Math.sqrt;

import org.apache.commons.math3.complex.Complex;

public class App 
{
	static final int REAL = 0;
    static final int IMAG = 1;
    
    public static void main( String[] args )
    {
		//Sampling rate
		int sampleRate = 44100;
		  
		// Frequency of the cosine wave in Hz
		float frequency = 440;
		  
		// Length of the cosine wave in samples
		int bufferSize = 4096;
		
		float[] input = new float[bufferSize];
		float[] hammingWindow = HammingWindow.generate(bufferSize);
		
		// Generate the cosine wave
		for (int i = 0; i < input.length; ++i) {
		    float t = i / (float)sampleRate;
		    input[i] = (float)Math.cos(2 * Math.PI * frequency * t);
		    input[i] *= hammingWindow[i];
		}

		NoteExtractor noteExtractor = new NoteExtractor(sampleRate);
		
		float[] frequencies = noteExtractor.frequencies();
		float[] output = noteExtractor.allocOutput();
		
		noteExtractor.execute(input, output);
		
		for (int bin = 0; bin < output.length / 2; ++bin) {
		    float mag = (float)sqrt(output[2 * bin + REAL] * output[2 * bin + REAL] + output[2 * bin + IMAG] * output[2 * bin + IMAG]);
		    System.out.println("Frequency: " + frequencies[bin] + ": " + mag);
		}
	}
}
