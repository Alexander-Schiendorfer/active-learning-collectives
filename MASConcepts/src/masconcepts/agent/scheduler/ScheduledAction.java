package masconcepts.agent.scheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import masconcepts.time.DiscreteCalendar;
import masconcepts.time.TimeInterval;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utilities.datastructures.NullObject;

/**
 * A scheduled method that can be executed after a specified time. The Execution uses reflection to specify the concrete
 * method and caller. The method that should be invoked has to be visible from outside the class (no private modifiers).
 *
 * @author Oliver
 *
 * @param <R>
 *            the return type of the object {@link #scheduledExecute(Date, Object, Object...)} returns.
 */
public class ScheduledAction<R> implements IScheduledAction<R> {

	/**
	 * For logging purposes.
	 */
	private final static Logger LOG = LogManager.getLogger(ScheduledAction.class.getSimpleName());

	/**
	 * The last time the {@link ScheduledAction}'s method was called.
	 */
	private Date lastTimeExecuted;

	/**
	 * The {@link ScheduledAction}'s method name for the reflection call.
	 */
	private final String methodName;

	/**
	 * List of parameters that has to be entered for calling the method addressed with this {@link ScheduledAction}.
	 */
	private final List<Class<?>> paramTypes;

	/**
	 * A map that maps method signatures to an object array consisting of a method and a list of parameter classes.
	 * <ul>
	 * <li>aMapEntry[0]: the method of type {@link Method}</li>
	 * <li>aMapEntry[1]: the corresponding list of parameter classes of type <code>Class[]</code></li>
	 * </ul>
	 */
	private final HashMap<String, Object[]> methodSignatureToMethodMap = new HashMap<String, Object[]>();

	/**
	 * Holds the initial value of {@link #lastTimeExecuted} as well as a time pattern that represents the frequency with
	 * which {@link #methodName} has to be called.
	 */
	private final TimeInterval initialTimeInterval;

	/**
	 * Generates a new {@link ScheduledAction} with a qualified name and frequency.
	 *
	 * @param currentCalendar
	 *            the {@link DiscreteCalendar} valid at the point in time the {@link ScheduledAction} is created
	 * @param methodName
	 *            the name of the remotely called method; cannot be changed during system run
	 * @param executionFrequencyInMinutes
	 *            the frequency the method should be called according to the specific scheduler; can not be changed
	 *            during system run
	 * @param parameters
	 *            the parameter classes the method signature contains
	 */
	public ScheduledAction(DiscreteCalendar currentCalendar, String methodName, long executionFrequencyInMinutes, Class<?>... parameters) {
		Date previousDateOnPattern = DiscreteCalendar.getPreviousDateOnPattern(currentCalendar.getDate(), (int) executionFrequencyInMinutes);

		this.initialTimeInterval = new TimeInterval(previousDateOnPattern, (int) executionFrequencyInMinutes);
		this.lastTimeExecuted = previousDateOnPattern;
		this.methodName = methodName;

		this.paramTypes = new ArrayList<Class<?>>();
		for (int i = 0; i < parameters.length; i++) {
			this.paramTypes.add(parameters[i]);
		}
	}

	/**
	 * Scheduled call of a specified method on a specified target object using reflection
	 *
	 * @param currentTime
	 *            the time when the method is called
	 * @param target
	 *            the target object the method is called on
	 * @param parameters
	 *            the list of parameters for the specified method.
	 * @return the return statement of the called method; is <code>null</code> if the return value should be void
	 * @throws ScheduledActionFrequencyException
	 *             throws a {@link ScheduledActionFrequencyException} if the method is called in a higher frequency than
	 *             specified in {@link #initialTimeInterval}.
	 */
	@Override
	public R scheduledExecute(Date currentTime, Object target, Object... parameters) throws ScheduledActionFrequencyException {
		return this.scheduledExecute(currentTime, target.getClass(), target, parameters);
	}

	/**
	 * Scheduled call of a specified method on a specified target object using reflection
	 *
	 * @param currentTime
	 *            the time when the method is called
	 * @param targetClass
	 *            the class the target is to be cast to
	 * @param target
	 *            the target object the method is called on
	 * @param parameters
	 *            the list of parameters for the specified method.
	 * @return the return statement of the called method; is <code>null</code> if the return value should be void
	 * @throws ScheduledActionFrequencyException
	 *             throws a {@link ScheduledActionFrequencyException} if the method is called in a higher frequency than
	 *             specified in {@link #initialTimeInterval}.
	 */
	public R scheduledExecute(Date currentTime, Class<?> targetClass, Object target, Object... parameters) throws ScheduledActionFrequencyException {
		if (!targetClass.isAssignableFrom(target.getClass()))
			throw ScheduledAction.LOG.throwing(new IllegalArgumentException("The target " + target + " is not of type " + targetClass + "!"));

		// The scheduled action should only be called if the current time is on the specified time pattern.
		if (!this.lastTimeExecuted.equals(currentTime) && this.initialTimeInterval.isOnTimePattern(currentTime)) {

			// update the initialization time
			this.lastTimeExecuted = new Date(currentTime.getTime());

			String methodSignature = "";

			// retrieve the parameter types for method identification
			Class<?> paramTypes_temp[] = new Class[parameters.length];
			for (int i = 0; i < paramTypes_temp.length; i++) {
				Class<?> paramClass = null;
				if (parameters[i] instanceof NullObject<?>) {
					paramClass = ((NullObject<?>) parameters[i]).getParameterClass();
					parameters[i] = null;
				} else
					paramClass = parameters[i].getClass();

				if (this.paramTypes.get(i).isAssignableFrom(paramClass)) {
					paramTypes_temp[i] = paramClass;
					methodSignature += paramClass;
				} else {
					throw ScheduledAction.LOG.throwing(new IllegalArgumentException("The scheduled execute of " + this.methodName
							+ " was called with the wrong set of parameters! Expected: " + this.paramTypes.get(i) + " -- Seen: " + paramClass));
				}
			}

			// the method that will be invoked
			Method theMethod = null;
			// the type of parameters given to the method
			Class<?> paramTypes[] = null;
			// the return value of the invoked method
			Object retValue = null;

			// indicate whether the method to call was found
			boolean cachedMethodFound = false;
			boolean notCachedMethodFound = false;

			// look whether method is in cache
			if (this.methodSignatureToMethodMap.containsKey(methodSignature)) {
				Object[] methodSignatureEntry = this.methodSignatureToMethodMap.get(methodSignature);
				theMethod = (Method) methodSignatureEntry[0];
				paramTypes = (Class[]) methodSignatureEntry[1];
				cachedMethodFound = true;
			} else { // method is not in cache -- search for it in target
				// get all methods of the target
				Method[] publicMethods = targetClass.getMethods();
				Method[] privateMethods = targetClass.getDeclaredMethods();
				List<Method> allMethods = new ArrayList<Method>(Arrays.asList(publicMethods));
				allMethods.addAll(Arrays.asList(privateMethods));

				// search for the appropriate method in methods
				for (Method method : allMethods) {
					if (method.getName().equals(this.methodName) && method.getParameterTypes().length == paramTypes_temp.length) {
						paramTypes = method.getParameterTypes();
						notCachedMethodFound = true;
						theMethod = method;
						// check whether all parameters are appropriate
						for (int i = 0; i < paramTypes_temp.length; i++) {
							if (!paramTypes[i].isAssignableFrom(paramTypes_temp[i])) {
								notCachedMethodFound = false;
								break;
							}
						}

						// method found
						if (notCachedMethodFound) {
							// put method in cache
							this.methodSignatureToMethodMap.put(methodSignature, new Object[] { theMethod, paramTypes });
							break;
						}
					}
				}
			}

			// if appropriate method was found, try to invoke it
			if (cachedMethodFound || notCachedMethodFound) {
				try {
					// enable access to private methods
					theMethod.setAccessible(true);
					// invoke method on target object
					if (parameters.length > 0) {
						retValue = theMethod.invoke(target, parameters);
					} else {
						retValue = theMethod.invoke(target);
					}
				} catch (IllegalArgumentException e) {
					throw ScheduledAction.LOG.throwing(new RuntimeException("Error while invoking method (method: " + theMethod
							+ "): the underlying method threw an exception", e.getCause()));
				} catch (IllegalAccessException e) {
					throw ScheduledAction.LOG.throwing(new RuntimeException("Error while invoking method (method: " + theMethod
							+ "): the underlying method threw an exception", e.getCause()));
				} catch (InvocationTargetException e) {
					throw ScheduledAction.LOG.throwing(new RuntimeException("Error while invoking method (method: " + theMethod
							+ "): the underlying method threw an exception", e.getCause()));
				}

			} else { // no appropriate method was found -- throw exception
				String paras = "";
				for (int i = 0; i < paramTypes_temp.length; i++)
					paras += paramTypes_temp[i] + ", ";
				throw ScheduledAction.LOG.throwing(new RuntimeException("Error while invoking method (method: " + this.methodName + ", given parameters: ("
						+ paras + "): no such method."));
			}

			// cast to required type
			@SuppressWarnings("unchecked")
			R retValue2 = (R) retValue;
			return retValue2;
		}

		// method was not called because the current date is not on the desired time pattern or the method has been
		// called for this time step before
		else {
			if (this.lastTimeExecuted.equals(currentTime)) {
				ScheduledAction.LOG.debug("Method " + this.methodName + " has been called for this time step before.");
				throw new ScheduledActionFrequencyException("Error while invoking method (method: " + this.methodName
						+ "). The method has been called for this time step before.");
			} else {
				ScheduledAction.LOG.debug("Method " + this.methodName
						+ " has not been called since the current time is not on this ScheduledAction's time pattern ("
						+ this.initialTimeInterval.getTimePatternInMinutes() + " min).");
				throw new ScheduledActionFrequencyException("Error while invoking method (method: " + this.methodName
						+ "). The current time is not on this ScheduledAction's time pattern (" + this.initialTimeInterval.getTimePatternInMinutes() + " min).");
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.methodName == null) ? 0 : this.methodName.hashCode());
		result = prime * result + ((this.paramTypes == null) ? 0 : this.paramTypes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		ScheduledAction<?> other = (ScheduledAction<?>) obj;
		if (this.methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!this.methodName.equals(other.methodName))
			return false;
		if (this.paramTypes == null) {
			if (other.paramTypes != null)
				return false;
		} else if (!this.paramTypes.equals(other.paramTypes))
			return false;
		return true;
	}
}