package masconcepts.agent.scheduler;

/**
 * TODO: document
 */
public class ScheduledActionFrequencyException extends Exception {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = 4360279990687886948L;

	/**
	 * TODO: document
	 * 
	 * @param errorMessage
	 */
	public ScheduledActionFrequencyException(String errorMessage) {
		super(errorMessage);

		// System.err.println(errorMessage);
		// System.err.println("Call frequency is higher than frequency in scheduler!");
		// this.printStackTrace();

		// TODO: remove System.exit(0) as soon as the resoluation of time steps is more fine-grained.
		// System.exit(0);
	}
}