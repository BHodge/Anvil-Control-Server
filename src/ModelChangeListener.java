import java.io.File;

/**
 * This class defines the required functions that the model
 * expects a 'view' to be able to handle.
 * @author BMCJ
 */
public interface ModelChangeListener {

	void changedQLFiles(File[] files);
	void changedStatus(boolean running, boolean connected);
	void changedClientIP(String client_ip);
	void changedServerIP(String server_ip);
	void changedServerName(String serv_name);
	void changedServerPswd(boolean psw_set);
	void changedBroadcast(boolean enabled);
}
