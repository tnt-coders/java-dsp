package log.charter.dsp;

public class App 
{	
    public static void main( String[] args )
    {
		//Sampling rate
		int sampleRate = 44100;
		  
		// Frequency of the cosine wave in Hz
		float frequency = 440;
		  
		// Length of the cosine wave in samples
		int bufferSize = 4096;
		
		float[] input = new float[bufferSize];
		
		// Generate the cosine wave
		for (int i = 0; i < input.length; ++i) {
		    float t = i / (float)sampleRate;
		    input[i] = (float)Math.cos(2 * Math.PI * frequency * t);
		}

		NoteExtractor noteExtractor = new NoteExtractor(bufferSize, sampleRate);
		
		float[] frequencies = noteExtractor.frequencies();
		Complex[] output = noteExtractor.allocOutput();
		
		noteExtractor.execute(input, output);
		
		for (int bin = 0; bin < output.length; ++bin) {
		    float mag = (float)Math.sqrt(output[bin].real * output[bin].real + output[bin].imag * output[bin].imag);
		    System.out.println("BIN: " + bin + " Frequency: " + frequencies[bin] + ": " + mag);
		}
	}
}
