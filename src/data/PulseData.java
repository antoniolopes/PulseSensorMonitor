package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class PulseData implements Serializable {

	private ArrayList<PulseSample> samples;
	private static final int BPM_TIME = 60;
	public static final int THRESH = 512;

	public PulseData() {
		super();
		this.samples = new ArrayList<PulseSample>();
	}

	public List<PulseSample> getSamples() {
		return samples;
	}

	public void setSamples(ArrayList<PulseSample> samples) {
		this.samples = samples;
	}

	public void addSample(int value, long timestamp) {
		this.samples.add(new PulseSample(value, timestamp));
	}

	public int getNrSamples() {
		return this.samples.size();
	}

	public int getDataSample(int index) {
		return this.samples.get(index).getValue();
	}

	public int getLastDataSampleValue() {
		return getDataSample(getNrSamples() - 1);
	}

	public int getBPM(long starting_time) {
		int bpm_time = BPM_TIME;

		long curr_time = System.currentTimeMillis();
		if (starting_time > (curr_time - BPM_TIME * 1000))
			bpm_time = (int) (curr_time - starting_time) / 1000;

		int nr_samples = getNrSamplesFromLastSeconds(bpm_time);
		System.out.println(nr_samples);
		return 60 * nr_samples / bpm_time;
	}

	private int getNrSamplesFromLastSeconds(int bpm_time) {
		long limit = System.currentTimeMillis() - (bpm_time * 1000);
		int bpm_samples = 0;
		boolean pulse = false;
		for (int i = this.samples.size() - 1; i >= 0 && this.samples.get(i).getTimestamp() > limit; i--) {
			if (this.samples.get(i).getValue() >= THRESH) {
				if (pulse == false) {
					bpm_samples++;
					pulse = true;
				}
			} else {
				pulse = false;
			}
		}
		return bpm_samples;
	}

	public int getMaxDataSample() {
		// TODO
		return 0;
	}

	public int getMinDataSample() {
		// TODO
		return 0;
	}

}
