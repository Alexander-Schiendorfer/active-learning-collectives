package masconcepts.agent.scheduler;

import java.util.Date;

import masconcepts.time.DiscreteCalendar;

/***
 * A kind of {@link ScheduledAction}, where the target is already defined via constructor. Calls the contained
 * {@link ScheduledAction#scheduledExecute(Date, Object, Object...)} using the stored target.
 * 
 * @author Oliver
 * 
 * @param <R>
 *            the return type of the {@link ScheduledAction#scheduledExecute(Date, Object, Object...)}
 */
public class TargetedScheduledAction<R extends Object> {

	/**
	 * The stored target the {@link TargetedScheduledAction#scheduledActionForTarget} is called on.
	 */
	private final Object target;

	/**
	 * The {@link ScheduledAction}.
	 */
	private final ScheduledAction<R> scheduledActionForTarget;

	/**
	 * The class the target is to be cast to when {@link #scheduledExecute(Date, Object...)} is called.
	 */
	private final Class<?> targetClass;

	/***
	 * Creates a new {@link TargetedScheduledAction} that is bound to a specific target. Apart from that, equivalent to
	 * {@link ScheduledAction}.
	 * 
	 * @param currentCalendar
	 *            the {@link DiscreteCalendar} valid at the point in time the {@link TargetedScheduledAction} is created
	 * @param targetClass
	 *            the class the target is to be cast to when {@link #scheduledExecute(Date, Object...)} is called.
	 * @param target
	 *            the target for scheduledExecute of {@link TargetedScheduledAction#scheduledActionForTarget}
	 * @param methodName
	 *            see {@link ScheduledAction}
	 * @param executionFrequencyInMinutes
	 *            see {@link ScheduledAction}
	 * @param parameters
	 *            see {@link ScheduledAction}
	 */
	public TargetedScheduledAction(DiscreteCalendar currentCalendar, Class<?> targetClass, Object target, String methodName, long executionFrequencyInMinutes,
			Class<?>... parameters) {
		this.target = target;
		this.scheduledActionForTarget = new ScheduledAction<R>(currentCalendar, methodName, executionFrequencyInMinutes, parameters);
		this.targetClass = targetClass;
	}

	/**
	 * Scheduled execute of {@link TargetedScheduledAction#scheduledActionForTarget}.
	 * 
	 * @param currentTime
	 *            see {@link ScheduledAction#scheduledExecute(Date, Object, Object...)}
	 * @return see {@link ScheduledAction#scheduledExecute(Date, Object, Object...)}
	 * @throws ScheduledActionFrequencyException
	 *             see {@link ScheduledAction#scheduledExecute(Date, Object, Object...)}
	 */
	public R scheduledExecute(Date currentTime, Object... parameters) throws ScheduledActionFrequencyException {
		return this.scheduledActionForTarget.scheduledExecute(currentTime, this.targetClass, this.target, parameters);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.scheduledActionForTarget == null) ? 0 : this.scheduledActionForTarget.hashCode());
		result = prime * result + ((this.target == null) ? 0 : this.target.hashCode());
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
		TargetedScheduledAction other = (TargetedScheduledAction) obj;
		if (this.scheduledActionForTarget == null) {
			if (other.scheduledActionForTarget != null)
				return false;
		} else if (!this.scheduledActionForTarget.equals(other.scheduledActionForTarget))
			return false;
		if (this.target == null) {
			if (other.target != null)
				return false;
		} else if (!this.target.equals(other.target))
			return false;
		return true;
	}
}
