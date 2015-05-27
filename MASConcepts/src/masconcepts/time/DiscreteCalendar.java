package masconcepts.time;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import utilities.parameters.SimulationParameters;

/**
 * A calendar that uses discrete time steps.
 */
public class DiscreteCalendar implements Cloneable, Serializable {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = -7891142123881599802L;

	/**
	 * A {@link GregorianCalendar} holding the initial value, i.e., the simulation start date, of {@link #theCalendar}.
	 */
	private static final GregorianCalendar INITIAL_CALENDAR = DiscreteCalendar.parseSimulationStartDate();

	/**
	 * Holds the minutes {@link #theCalendar} is incremented by when the time is updated.
	 */
	public static final int MINUTES_PER_TICK = SimulationParameters.getIntParameter("discreteCalendar.minutesPerTick", 15);

	/**
	 * The underlying {@link GregorianCalendar}. Holds the current time in form of a {@link GregorianCalendar} which is
	 * incremented by {@link #MINUTES_PER_TICK} each time {@link #incrementTickAndTime()} is called.
	 */
	private final GregorianCalendar theCalendar;

	/**
	 * Holds the current tick of the simulation that is incremented by 1 each time {@link #incrementTickAndTime()} is
	 * called.
	 */
	private int currentTick;

	/**
	 * Indicates whether the system should avoid a given time interval (see {@link #avoidTimeIntervalStartingHour} and
	 * {@link #avoidTimeIntervalDuration}), i.e., this time interval is skipped when incrementing the
	 * {@link #theCalendar}.
	 */
	private final boolean avoidTimeInterval;

	/**
	 * Holds the hour (i.e., a value between 0 and 23!) of the time interval's start time which has to be skipped when
	 * {@link #avoidTimeInterval} is <code>true</code>.
	 */
	private final int avoidTimeIntervalStartingHour;

	/**
	 * Holds the duration (in hours) of the time interval which has to be skipped when {@link #avoidTimeInterval} is
	 * <code>true</code>.
	 */
	private final int avoidTimeIntervalDuration;

	/**
	 * Creates a new {@link DiscreteCalendar}.
	 *
	 * @param currentTick
	 *            the simulation's current tick
	 */
	public DiscreteCalendar(int currentTick) {
		// current tick
		this.currentTick = currentTick;

		// Determine current date
		this.theCalendar = new GregorianCalendar();
		this.theCalendar.setTime(DiscreteCalendar.getDateForDiscreteTimeStepRelativeToGivenTime(DiscreteCalendar.INITIAL_CALENDAR.getTime(), currentTick,
				DiscreteCalendar.MINUTES_PER_TICK));

		// avoid time interval
		this.avoidTimeInterval = SimulationParameters.getBoolean("discreteCalendar.avoidinterval", false);
		this.avoidTimeIntervalStartingHour = SimulationParameters.getIntParameter("discreteCalendar.avoidinterval.start", 19);
		this.avoidTimeIntervalDuration = SimulationParameters.getIntParameter("discreteCalendar.avoidinterval.duration", 12);

		if (this.avoidTimeInterval && (this.avoidTimeIntervalStartingHour < 0 || this.avoidTimeIntervalStartingHour > 23))
			throw new IllegalArgumentException("The hour when the avoided time interval should start must be between 0 and 23!");
	}

	/**
	 * Increments {@link #currentTick} by 1 and {@link #theCalendar} by {@link #MINUTES_PER_TICK}.
	 */
	public void incrementTickAndTime() {
		this.currentTick++;

		// update time
		if (!this.avoidTimeInterval) {
			// increment time by minutesPerTick
			this.theCalendar.add(Calendar.MINUTE, DiscreteCalendar.MINUTES_PER_TICK);
		} else {
			// jump from (AVOID_TIME_INTERVAL_START-1):45 to (current time + AVOID_TIME_INTERVAL_DURATION)
			GregorianCalendar calendarClone = this.getCalendar();
			calendarClone.set(Calendar.HOUR_OF_DAY, this.avoidTimeIntervalStartingHour - 1);
			calendarClone.set(Calendar.MINUTE, 45);

			if (this.theCalendar.getTime().getTime() == calendarClone.getTime().getTime()) {
				this.theCalendar.add(Calendar.HOUR, this.avoidTimeIntervalDuration);
			} else {
				this.theCalendar.add(Calendar.MINUTE, DiscreteCalendar.MINUTES_PER_TICK);
			}
		}
	}

	/**
	 * Returns the {@link Date} on the given <code>timePattern</code> which is <code>numberOfDiscreteTimeSteps</code>
	 * discrete time steps away from the <b>current time</b>.
	 *
	 * TODO: does not work when (minutesPerTick % timePattern != 0) && (timePattern % minutesPerTick != 0): ceil or
	 * floor dates according to the timePattern
	 *
	 * @param numberOfDiscreteTimeSteps
	 *            the number of discrete time steps away from the current time on the given <code>timePattern</code>.
	 *            Note that this value can also be negative to get {@link Date}s before the current time.
	 * @param timePattern
	 *            the time pattern in <b>minutes</b>
	 * @return the {@link Date} on the given <code>timePattern</code> which is <code>numberOfDiscreteTimeSteps</code>
	 *         discrete time steps away from the <b>current time</b>
	 */
	public Date getDateForDiscreteTimeStepRelativeToCurrentTime(int numberOfDiscreteTimeSteps, int timePattern) {
		return DiscreteCalendar.getDateForDiscreteTimeStepRelativeToGivenTime(this.getCalendar().getTime(), numberOfDiscreteTimeSteps, timePattern);
	}

	/**
	 * Returns the {@link Date} on the given <code>timePattern</code> which is <code>numberOfDiscreteTimeSteps</code>
	 * discrete time steps away from the <b>given time</b>.
	 *
	 * @param date
	 *            the {@link Date} of which the discrete time steps are
	 * @param numberOfDiscreteTimeSteps
	 *            the number of discrete time steps away from the current time on the given <code>timePattern</code>.
	 *            Note that this value can also be negative to get {@link Date}s before the given time.
	 * @param timePattern
	 *            the time pattern in <b>minutes</b>
	 * @return the {@link Date} on the given <code>timePattern</code> which is <code>numberOfDiscreteTimeSteps</code>
	 *         discrete time steps away from the <b>given time</b>
	 */
	public static Date getDateForDiscreteTimeStepRelativeToGivenTime(Date date, int numberOfDiscreteTimeSteps, int timePattern) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, numberOfDiscreteTimeSteps * timePattern);
		return cal.getTime();
	}

	/**
	 * Returns the next {@link Date} after the given <code>date</code> that fits in with the specified
	 * <code>timePattern</code> starting at the date specified by the {@link #INITIAL_CALENDAR}.
	 *
	 * @param date
	 *            a {@link Date} before the created {@link Date} (does not have to fit in with the specified time
	 *            pattern)
	 * @param timePattern
	 *            the time pattern in <b>minutes</b>
	 * @return the created {@link Date}
	 */
	public static Date getNextDateOnPattern(Date date, int timePattern) {
		int nbOfDiscreteTimeSteps = (int) ((date.getTime() - DiscreteCalendar.INITIAL_CALENDAR.getTime().getTime()) / (timePattern * 60 * 1000) + 1);

		return DiscreteCalendar.getDateForDiscreteTimeStepRelativeToGivenTime(DiscreteCalendar.INITIAL_CALENDAR.getTime(), nbOfDiscreteTimeSteps, timePattern);
	}

	/**
	 * Returns the next {@link Date} before the given <code>date</code> that fits in with the specified
	 * <code>timePattern</code> starting at the date specified by the {@link #INITIAL_CALENDAR}.
	 *
	 * @param date
	 *            a {@link Date} after the created {@link Date} (does not have to fit in with the specified time
	 *            pattern)
	 * @param timePattern
	 *            the time pattern in <b>minutes</b>
	 * @return the created {@link Date}
	 */
	public static Date getPreviousDateOnPattern(Date date, int timePattern) {
		int nbOfDiscreteTimeSteps = (int) ((date.getTime() - DiscreteCalendar.INITIAL_CALENDAR.getTime().getTime()) / (timePattern * 60 * 1000));

		if (DiscreteCalendar.isOnTimePattern(date, timePattern))
			nbOfDiscreteTimeSteps = nbOfDiscreteTimeSteps - 1;

		return DiscreteCalendar.getDateForDiscreteTimeStepRelativeToGivenTime(DiscreteCalendar.INITIAL_CALENDAR.getTime(), nbOfDiscreteTimeSteps, timePattern);
	}

	/**
	 * Calculates the simulation tick for a given <code>date</code>. As the date may not lie on the
	 * {@link DiscreteCalendar}'s time pattern (see {@link #MINUTES_PER_TICK}), the returned tick may be a decimal
	 * value!
	 *
	 * @param date
	 *            the {@link Date} to get the tick for
	 * @return the tick for the given <code>date</code>
	 */
	public static double getTickForDate(Date date) {
		double ticksBetweenDates = ((date.getTime() - DiscreteCalendar.INITIAL_CALENDAR.getTime().getTime()) / (DiscreteCalendar.MINUTES_PER_TICK * 60.0 * 1000));
		return ticksBetweenDates;
	}

	/**
	 * Creates a new {@link TimeInterval} <b>relative to the current time step</b>, i.e., a
	 * <code>startingTimeStep</code> of 1 and <code>endingTimeStep</code> of 2 creates a {@link TimeInterval} for the
	 * next and the next but one step on the <code>timePattern</code>.
	 *
	 * TODO: does not work when (minutesPerTick % timePattern != 0) && (timePattern % minutesPerTick != 0): ceil or
	 * floor dates according to the timePattern
	 *
	 * @param startingTimeStep
	 *            the discrete time step relative to the current time step. Note that this value can be negative, too.
	 * @param endingTimeStep
	 *            the discrete time step relative to the current time step (must not be smaller than
	 *            <code>startingTimeStep</code>). Note that this value can be negative, too.
	 * @param timePattern
	 *            the time pattern in <b>minutes</b>
	 * @return a new {@link TimeInterval} <b>relative to the current time step</b> for the given steps and with the
	 *         given time pattern
	 */
	public TimeInterval createTimeIntervalFromDiscreteTimeStepToDiscreteTimeStep(int startingTimeStep, int endingTimeStep, int timePattern) {
		if (startingTimeStep > endingTimeStep)
			throw new IllegalArgumentException("The starting time step must not be after the ending time step!");
		Date startDate = this.getDateForDiscreteTimeStepRelativeToCurrentTime(startingTimeStep, timePattern);
		Date endDate = this.getDateForDiscreteTimeStepRelativeToCurrentTime(endingTimeStep, timePattern);
		return new TimeInterval(startDate, endDate, timePattern);
	}

	/**
	 * Checks whether or not the given {@link Date} is on the given <code>timePattern</code> (relative to the given
	 * {@link Date}).
	 *
	 * @param date
	 *            the {@link Date} to check
	 * @param relativeToDate
	 *            the {@link Date} the pattern is originates from
	 * @param timePattern
	 *            the time pattern in <b>minutes</b>
	 * @return <code>true</code> if the {@link Date} is on the time pattern, <code>false</code> otherwise
	 */
	public static boolean isOnTimePattern(Date date, Date relativeToDate, int timePattern) {
		return (date.getTime() - relativeToDate.getTime()) % (timePattern * 60 * 1000) == 0;
	}

	/**
	 * Checks whether or not the given {@link Date} is on the given <code>timePattern</code> (relative to the
	 * {@link #INITIAL_CALENDAR}).
	 *
	 * @param date
	 *            the {@link Date} to check
	 * @param timePattern
	 *            the time pattern in <b>minutes</b>
	 * @return <code>true</code> if the {@link Date} is on the time pattern, <code>false</code> otherwise
	 */
	public static boolean isOnTimePattern(Date date, int timePattern) {
		return DiscreteCalendar.isOnTimePattern(date, DiscreteCalendar.INITIAL_CALENDAR.getTime(), timePattern);
	}

	/**
	 * Checks whether or not the given {@link TimeInterval} is on the given <code>timePattern</code> relative to the
	 * {@link #INITIAL_CALENDAR}).
	 *
	 * @param ti
	 *            the {@link TimeInterval} to check
	 * @param timePattern
	 *            the time pattern in <b>minutes</b>
	 * @return <code>true</code> if the {@link TimeInterval} is on the time pattern, <code>false</code> otherwise
	 */
	public static boolean isOnTimePattern(TimeInterval ti, int timePattern) {
		boolean isOnTimePattern = (ti.getStartDate().getTime() - DiscreteCalendar.INITIAL_CALENDAR.getTime().getTime()) % (timePattern * 60 * 1000) == 0;
		isOnTimePattern = isOnTimePattern
				&& (ti.getEndDate().getTime() - DiscreteCalendar.INITIAL_CALENDAR.getTime().getTime()) % (timePattern * 60 * 1000) == 0;
		isOnTimePattern = isOnTimePattern && (ti.getTimePatternInMinutes() % timePattern == 0);

		return isOnTimePattern;
	}

	/**
	 * Checks whether or not the given tick is on the given <code>timePattern</code> (relative to the first tick).
	 *
	 * @param tick
	 *            the tick to check
	 * @param timePattern
	 *            the time pattern in <b>minutes</b>
	 * @return <code>true</code> if the tick is on the time pattern, <code>false</code> otherwise
	 */
	public static boolean isOnTimePattern(int tick, int timePattern) {
		return (tick * DiscreteCalendar.MINUTES_PER_TICK) % timePattern == 0;
	}

	/**
	 * Gets the number of minutes between <code>sinceDate</code> and <code>toDate</code> (<code>toDate</code> must not
	 * be before <code>sinceDate</code>).
	 *
	 * @param sinceDate
	 *            the earlier {@link Date}
	 * @param toDate
	 *            the later {@link Date}
	 * @return the number of minutes between <code>sinceDate</code> and <code>toDate</code>
	 */
	public static int getMinutesBetweenDates(Date sinceDate, Date toDate) {
		if (toDate.before(sinceDate))
			throw new IllegalArgumentException("toDate must not be before sinceDate (toDate: " + toDate + ", sinceDate: " + sinceDate + ")!");

		return (int) ((toDate.getTime() - sinceDate.getTime()) / (60 * 1000));
	}

	/**
	 * Converts a time given in ms into a time in min.
	 *
	 * @param timeInMs
	 * @return
	 */
	public static long msToMin(long timeInMs) {
		if (timeInMs % (60 * 1000) != 0) {
			throw new IllegalArgumentException("The time must always be a multiple of 1min!");
		}

		long timeInMin = timeInMs / (60 * 1000);
		return timeInMin;
	}

	/**
	 * Converts a time given in min into a time in ms.
	 *
	 * @param timeInMin
	 * @return
	 */
	public static long minToMs(long timeInMin) {
		long timeInMs = timeInMin * (60 * 1000);
		return timeInMs;
	}

	/**
	 * @return Returns a clone of {@link #theCalendar}.
	 */
	public GregorianCalendar getCalendar() {
		return (GregorianCalendar) this.theCalendar.clone();
	}

	/**
	 * @return Returns a cloned {@link Date} of {@link #theCalendar}'s current time.
	 */
	public Date getDate() {
		return new Date(this.theCalendar.getTime().getTime());
	}

	/**
	 * @return Returns {@link #currentTick}.
	 */
	public int getCurrentTick() {
		return this.currentTick;
	}

	/**
	 * @return Returns the simulation start date in the form of a {@link GregorianCalendar}
	 */
	public static GregorianCalendar getSimulationStartDate() {
		return (GregorianCalendar) DiscreteCalendar.INITIAL_CALENDAR.clone();
	}

	/**
	 * Parses the simulation start date in the form of a {@link GregorianCalendar} from the batch parameters.
	 *
	 * @return the simulation start date in the form of a {@link GregorianCalendar}
	 */
	private static GregorianCalendar parseSimulationStartDate() {
		GregorianCalendar startCalendar = new GregorianCalendar(2010, 3, 12, 7, 0, 0); // default: 12.04.2010, 07:00 AM
		if (SimulationParameters.exists("discreteCalendar.startDate")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				startCalendar.setTime(sdf.parse(SimulationParameters.getString("discreteCalendar.startDate")));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return (GregorianCalendar) startCalendar.clone();
	}

	@Override
	public DiscreteCalendar clone() {
		// create a new discrete calendar with the current tick (the other parameters are fix)
		return new DiscreteCalendar(this.currentTick);
	}

	@Override
	public String toString() {
		return "DiscreteCalendar [currentDate=" + this.getDate() + "]";
	}
}