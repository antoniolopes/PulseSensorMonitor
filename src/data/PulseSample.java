package data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PulseSample implements Serializable {

	private int value;
	private long timestamp;

	public PulseSample(int value, long timestamp) {
		super();
		this.value = value;
		this.timestamp = timestamp;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
