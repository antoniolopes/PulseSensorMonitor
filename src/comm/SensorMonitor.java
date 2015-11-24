package comm;

public interface SensorMonitor {

	public void initMonitor();

	public void stopMonitor();

	public void newDataReceived(String data);

	public void errorReceived(String error);

	public void infoMessageReceived(String message);
}
