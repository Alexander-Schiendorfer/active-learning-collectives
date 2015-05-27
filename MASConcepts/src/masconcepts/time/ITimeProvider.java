package masconcepts.time;

import java.util.Date;

/**
 * An interface that provides a method to query the current {@link Date} or a {@link DiscreteCalendar}.
 * 
 * @author Gerrit
 * 
 */
public interface ITimeProvider {

	/**
	 * Returns the current time in form of a {@link DiscreteCalendar}.
	 * 
	 * @return
	 */
	public DiscreteCalendar getDiscreteCalendar();

	/**
	 * Returns the current {@link Date}.
	 */
	public Date getCurrentDate();

	/**
	 * Initializes this {@link ITimeProvider}.
	 */
	public void initTimeProvider();

	/**
	 * Dissolves this {@link ITimeProvider}.
	 */
	public void finalizeTimeProvider();

}