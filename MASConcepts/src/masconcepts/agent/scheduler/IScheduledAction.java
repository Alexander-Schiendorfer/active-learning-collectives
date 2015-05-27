package masconcepts.agent.scheduler;

import java.util.Date;

/**
 * Interface defining an action that can be periodically triggered.
 * 
 * @author Oliver
 * 
 * @param T
 *            the type of the object {@link #scheduledExecute(Date, Object, Object...)} returns.
 */
public interface IScheduledAction<T> {

	/**
	 * Tries to perform this {@link IScheduledAction}.
	 * 
	 * @param currentTime
	 *            the time when the action is tried to be performed
	 * @param target
	 *            the target object the method is called on
	 * @param parameters
	 *            the parameters that are sent with the call
	 * @return the result of the action; is null if the return value should be void
	 * @throws Exception
	 */
	public T scheduledExecute(Date currentTime, Object target, Object... parameters) throws Exception;
}
