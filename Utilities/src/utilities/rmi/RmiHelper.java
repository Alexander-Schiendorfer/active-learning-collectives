package utilities.rmi;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides functionality for starting an terminating RMI hosts.
 *
 * @author Gerrit
 *
 */
public class RmiHelper {

	/**
	 * For logging purposes.
	 */
	private final static Logger LOG = LogManager.getLogger();

	/**
	 * Starts a new RMI host.
	 *
	 * @param defaultPort
	 * @param maxPortAttempts
	 * @param hostName
	 * @param host
	 * @return the port
	 * @throws AccessException
	 * @throws RemoteException
	 * @throws AlreadyBoundException
	 */
	public static int startRmiHost(final int defaultPort, final int maxPortAttempts, final String hostName, final UnicastRemoteObject host)
			throws AccessException, RemoteException, AlreadyBoundException {
		int port = RmiHelper.getPortForRmiHost(defaultPort, maxPortAttempts);
		RmiHelper.startRmiHostForPort(port, hostName, host);

		return port;
	}

	/**
	 * Determines a free port for a new RMI host.
	 *
	 * @param defaultPort
	 * @param maxPortAttempts
	 * @return the port
	 * @throws RemoteException
	 */
	public static int getPortForRmiHost(final int defaultPort, final int maxPortAttempts) throws RemoteException {
		int rmiPort = defaultPort;
		int attempts = 0;
		while (attempts < maxPortAttempts) {
			try {
				// try to create the registry for the port
				LocateRegistry.createRegistry(rmiPort);
				break;
			} catch (Exception be) {
				// registry was already created -> look if the port is free, i.e., no other Remotes are bound to it
				// if so, use this port
				if (LocateRegistry.getRegistry(rmiPort).list().length == 0)
					break;
				// port is not free: try the next one
				rmiPort++;
				attempts++;
			}
		}

		if (attempts >= maxPortAttempts) {
			throw RmiHelper.LOG.throwing(new RemoteException("Cannot find open port!"));
		}

		return rmiPort;
	}

	/**
	 * Starts a new RMI host for the given port.
	 *
	 * @param port
	 * @param hostName
	 * @param host
	 * @return
	 * @throws AccessException
	 * @throws RemoteException
	 * @throws AlreadyBoundException
	 */
	public static void startRmiHostForPort(final int port, final String hostName, final UnicastRemoteObject host) throws AccessException, RemoteException,
	AlreadyBoundException {
		// start RMI server
		LocateRegistry.getRegistry(port).bind(hostName, host);
		RmiHelper.LOG.info("Created " + hostName + " as RMI Server on port " + port + ".");
	}

	/**
	 * Terminates the specified RMI host.
	 *
	 * @param port
	 * @param hostName
	 * @param host
	 * @throws AccessException
	 * @throws RemoteException
	 */
	public static void terminateRmiHost(int port, String hostName, UnicastRemoteObject host) throws AccessException, RemoteException {
		// end RMI server
		try {
			LocateRegistry.getRegistry(port).unbind(hostName);
		} catch (NotBoundException e) {
			RmiHelper.LOG.error(e);
		}
		UnicastRemoteObject.unexportObject(host, true);
	}

	/**
	 * Finds the specified RMI host.
	 *
	 * @param port
	 * @param hostName
	 * @return the specified RMI host
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static Remote rmiLookup(int port, String hostName) throws RemoteException, NotBoundException {
		Registry r = LocateRegistry.getRegistry(port);
		Remote remote = r.lookup(hostName);

		return remote;
	}
}
