package masconcepts.time;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Simple time interval of discrete points in time with immutable start and end dates. The interval is iterable,
 * returning one {@link Date} for each tick between the given dates (the end date is included).
 *
 * @author Jan-Philipp Steghöfer
 */
public class TimeInterval implements Serializable, Iterable<Date> {

	/**
	 * Unique identifier for serialization purposes.
	 */
	private static final long serialVersionUID = 5064204198719635619L;

	/**
	 * Holds the interval's start date.
	 */
	protected final Date startDate;

	/**
	 * Holds the interval's end date.
	 */
	protected final Date endDate;

	/**
	 * The time pattern in minutes used to define the discrete points in time.
	 */
	private final int timePattern;

	/**
	 * The discrete points in time between the {@link #startDate} (included) and {@link #endDate} (included).
	 */
	private Date[] discreteTimeSteps;

	/**
	 * Creates a so-called empty {@link TimeInterval}, i.e., a {@link TimeInterval} with
	 * <code>{@link #startDate} == null</code> and <code>{@link #endDate} == null</code>.
	 */
	private TimeInterval() {
		this(null, null, 0);
	}

	/**
	 * Creates a new time interval of length 1, i.e., an interval that starts and ends at <code>date</code>.
	 *
	 * @param date
	 *            the start and end date
	 * @param timePattern
	 *            The time pattern in minutes used to define the discrete points in time
	 */
	public TimeInterval(Date date, int timePattern) {
		this(date, date, timePattern);
	}

	/**
	 * Creates a new time interval that starts at <code>startDate</code> and ends at <code>endDate</code>.
	 *
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @param timePattern
	 *            The time pattern in minutes used to define the discrete points in time between the start date and the
	 *            end date. If start and end date are <code>null</code>, the time pattern is ignored and set to zero.
	 */
	public TimeInterval(Date startDate, Date endDate, int timePattern) {
		if (!(startDate == null && endDate == null) && startDate.after(endDate))
			throw new IllegalArgumentException("startDate must be before or equal to endDate.");

		if (startDate == null && endDate == null) {
			this.startDate = null;
			this.endDate = null;
			this.timePattern = 0;
		} else {
			this.startDate = new Date(startDate.getTime());
			this.endDate = new Date(endDate.getTime());
			this.timePattern = timePattern;

			if (!startDate.equals(endDate)) {
				if (!this.isOnTimePattern(endDate))
					throw new IllegalArgumentException("startDate (" + startDate + ") and endDate (" + endDate
							+ ")  do not fit in with the specified timePattern (" + timePattern + ").");
			}
		}
	}

	/**
	 * Creates a new time interval that starts at <code>startDate</code> and ends at
	 * <code>startDate.getTime() + timePattern * (length - 1)</code>.
	 *
	 * @param startDate
	 *            the start date.
	 * @param timePattern
	 *            The time pattern in minutes used to define the discrete points in time between the start date and the
	 *            end date.
	 * @param length
	 *            The length of the resulting {@link TimeInterval}. Must be <code>> 0</code>.
	 * @return The created {@link TimeInterval}.
	 */
	public static TimeInterval createTimeInterval(Date startDate, int timePattern, int length) {
		if (startDate == null)
			throw new IllegalArgumentException("The date must not be null!");

		if (length <= 0)
			throw new IllegalArgumentException("The length must be >= 0!");

		if (length == 1)
			return new TimeInterval(startDate, timePattern);

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(startDate);
		cal.add(Calendar.MINUTE, timePattern * (length - 1));

		return new TimeInterval(startDate, cal.getTime(), timePattern);
	}

	/**
	 * Creates a so-called empty {@link TimeInterval}, i.e., a {@link TimeInterval} with
	 * <code>{@link #startDate} == null</code> and <code>{@link #endDate} == null</code>.
	 *
	 * @return the empty {@link TimeInterval}
	 */
	public static TimeInterval createEmptyTimeInterval() {
		return new TimeInterval();
	}

	/**
	 * Gets the start date of the interval.
	 *
	 * @return the start date
	 */
	public Date getStartDate() {
		return this.startDate;
	}

	/**
	 * Gets the end date of the interval.
	 *
	 * @return the end date
	 */
	public Date getEndDate() {
		return this.endDate;
	}

	/**
	 *
	 * @return {@link #timePattern} in minutes.
	 */
	public int getTimePatternInMinutes() {
		return this.timePattern;
	}

	/**
	 *
	 * @return {@link #timePattern} in milliseconds.
	 */
	public long getTimePatternInMs() {
		return this.timePattern * 60 * 1000;
	}

	/**
	 *
	 * @return {@link #timePattern} in hours.
	 */
	public double getTimePatternInHours() {
		return this.timePattern / 60.0;
	}

	/**
	 * Checks if a date is on the time pattern.
	 *
	 * @param date
	 *            The date to check.
	 * @return <code>True</code> if the date is on the time pattern, else <code>false</code>.
	 */
	public boolean isOnTimePattern(Date date) {
		return (date.getTime() - this.startDate.getTime()) % this.getTimePatternInMs() == 0;
	}

	/**
	 * Returns <code>true</code> if this {@link TimeInterval} is empty, i.e., {@link #startDate} == <code>null</code>
	 * and {@link #endDate} == <code>null</code>.
	 */
	public boolean isEmptyTimeInterval() {
		return this.startDate == null && this.endDate == null;
	}

	/**
	 * Checks whether <code>otherInterval</code> is fully contained in this {@link TimeInterval}. <br/>
	 * If <code>otherInterval</code> is an empty interval, it is contained in this interval. Otherwise,
	 * <code>otherInterval</code> is contained in this interval if and only if its start date is not before this
	 * interval's start date and its end date is not after this interval's end date.
	 *
	 * @param otherInterval
	 * @return <code>true</code> if the given {@link TimeInterval} is contained in this {@link TimeInterval}.
	 */
	public boolean containsTimeInterval(TimeInterval otherInterval) {
		if (otherInterval.isEmptyTimeInterval() || (!this.endDate.before(otherInterval.endDate) && !this.startDate.after(otherInterval.startDate)))
			return true;

		return false;
	}

	/**
	 * Returns <code>true</code> if the given <code>date</code> is contained in this {@link TimeInterval} (
	 * {@link #startDate} and {@link #endDate} included).
	 *
	 * @param date
	 */
	public boolean containsDate(Date date) {
		if (!date.before(this.getStartDate()) && !date.after(this.getEndDate())) {
			return true;
		} else
			return false;
	}

	/**
	 * Returns <code>true</code> if the given {@link TimeInterval} <code>otherInterval</code> overlaps this
	 * {@link TimeInterval}.
	 *
	 * @param otherInterval
	 */
	public boolean overlaps(TimeInterval otherInterval) {
		if (otherInterval.endDate.before(this.startDate))
			return false;

		if (otherInterval.startDate.after(this.endDate))
			return false;

		return true;
	}

	/**
	 * Fills {@link #discreteTimeSteps} with the discrete points in time between the {@link #startDate} (included) and
	 * {@link #endDate} (included).
	 */
	private void setupDiscreteTimeSteps() {
		if (this.discreteTimeSteps == null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(this.startDate);
			Date nextDate = cal.getTime();

			List<Date> discreteTimeSteps = new ArrayList<Date>();

			while (!nextDate.after(this.endDate)) {
				discreteTimeSteps.add(nextDate);

				if (this.timePattern == 0)
					break;

				cal.add(Calendar.MINUTE, this.timePattern);
				nextDate = cal.getTime();
			}

			this.discreteTimeSteps = discreteTimeSteps.toArray(new Date[discreteTimeSteps.size()]);
		}
	}

	/**
	 * Returns the number of discrete points in time between the {@link #startDate} (included) and {@link #endDate}
	 * (included).
	 */
	public int getLength() {
		// setup discrete time steps information if not yet available
		if (this.discreteTimeSteps == null)
			this.setupDiscreteTimeSteps();

		return this.discreteTimeSteps.length;
	}

	/**
	 * Gets the i-th date within this {@link TimeInterval}.
	 *
	 * @param i
	 *
	 * @return the i-th date within this {@link TimeInterval}.
	 */
	public Date get(int i) {
		// setup discrete time steps information if not yet available
		if (this.discreteTimeSteps == null)
			this.setupDiscreteTimeSteps();

		return this.discreteTimeSteps[i];
	}

	@Override
	public Iterator<Date> iterator() {
		// setup discrete time steps information if not yet available
		if (this.discreteTimeSteps == null)
			this.setupDiscreteTimeSteps();

		return new Iterator<Date>() {

			private int cursor = 0;
			private int length = TimeInterval.this.getLength();

			@Override
			public boolean hasNext() {
				return this.cursor < this.length;
			}

			@Override
			public Date next() {
				if (this.hasNext()) {
					Date returnDate = TimeInterval.this.discreteTimeSteps[this.cursor++];
					return returnDate;
				} else
					throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 *
	 * @return all discrete {@link Date}s contained in this {@link TimeInterval}.
	 */
	public Collection<Date> containedDates() {
		if (this.discreteTimeSteps == null)
			this.setupDiscreteTimeSteps();

		HashSet<Date> containedDates = new HashSet<Date>(this.getLength());
		for (Date d : this.discreteTimeSteps)
			containedDates.add(d);

		return containedDates;
	}

	/**
	 * Returns a {@link List} of {@link TimeInterval}s for {@link Date}s that are not contained in the given
	 * {@link TimeInterval}, that is, returns the parts of this {@link TimeInterval} that do not overlap the given
	 * {@link TimeInterval}. In the normal case, this method should return a {@link List} of size 0 (if the given
	 * {@link TimeInterval} fully overlaps or is the same as this {@link TimeInterval}), of size 1 (if the given
	 * {@link TimeInterval} does not overlap this {@link TimeInterval} at all or only overlaps this {@link TimeInterval}
	 * on one side), or of size 2 (if this {@link TimeInterval} fully overlaps the given {@link TimeInterval}).
	 *
	 * @param otherInterval
	 * @return
	 */
	public List<TimeInterval> getTimeIntervalsNotOverlappingGivenTimeInterval(TimeInterval otherInterval) {
		if (this.timePattern != otherInterval.timePattern)
			throw new IllegalArgumentException("Time patterns must be equal!");

		List<TimeInterval> returnList = new ArrayList<TimeInterval>();

		// return at once "this" if both intervals do not overlap
		if (!this.overlaps(otherInterval)) {
			returnList.add(this);
			return returnList;
		}

		// get all dates that are not contained in otherInterval
		List<Date> notContainedDates = new ArrayList<Date>();
		for (Date d : this) {
			if (!otherInterval.containsDate(d)) {
				notContainedDates.add(d);
			}
		}

		// generate time intervals (in the normal case this should generate 1 or 2 time intervals, depending how "this"
		// and otherInterval overlap) for the dates not contained in otherInterval
		while (!notContainedDates.isEmpty()) {
			// create a temporary TimeInterval containing all dates not contained in otherInterval. Note that this may
			// again contain dates that are not in notContainedDates.
			TimeInterval temp_ti = new TimeInterval(notContainedDates.get(0), notContainedDates.get(notContainedDates.size() - 1), this.timePattern);
			Date startDate = temp_ti.getStartDate();
			Date endDate = temp_ti.getStartDate();
			// now search for the end date
			for (Date d : temp_ti) {
				if (notContainedDates.contains(d))
					endDate = d;
				else
					// this date is not in notContainedDates, so we found the endDate for the time interval
					break;
			}
			// remove the dates of the new TimeInterval from the notContainedDates to properly continue the while loop
			TimeInterval t = new TimeInterval(startDate, endDate, this.timePattern);
			for (Date d : t) {
				notContainedDates.remove(d);
			}
			returnList.add(t);
		}

		return returnList;
	}

	/**
	 *
	 * @param date
	 * @return <code>true</code> iff this interval's {@link #startDate} and {@link #endDate} equal the given
	 *         {@link Date}.
	 */
	public boolean equalsDate(Date date) {
		return date.equals(this.getStartDate()) && date.equals(this.getEndDate());
	}

	@Override
	public String toString() {
		return "Time Interval: [startDate = " + this.startDate + "] [endDate = " + this.endDate + "] [timePattern (in minutes) = " + this.timePattern + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.endDate == null) ? 0 : this.endDate.hashCode());
		result = prime * result + ((this.startDate == null) ? 0 : this.startDate.hashCode());
		result = prime * result + this.timePattern;
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
		TimeInterval other = (TimeInterval) obj;
		if (this.endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!this.endDate.equals(other.endDate))
			return false;
		if (this.startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!this.startDate.equals(other.startDate))
			return false;
		if (this.timePattern != other.timePattern)
			return false;
		return true;
	}
}