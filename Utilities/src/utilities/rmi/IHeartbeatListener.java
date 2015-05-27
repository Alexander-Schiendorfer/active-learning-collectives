package utilities.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link Remote} interface for RMI calls. Used by the clients to send a heartbeat message to their host.
 */
public interface IHeartbeatListener extends Remote {

	/**
	 * Sends a heartbeat message to an RMI host.
	 *
	 * @throws RemoteException
	 */
	public void sendHeartbeat() throws RemoteException;
}
