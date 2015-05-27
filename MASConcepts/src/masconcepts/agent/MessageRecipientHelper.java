package masconcepts.agent;

import java.util.Set;

/**
 * Class providing mechanisms that are useful in the context of dealing with {@link IMessageRecipient}s such as
 * registering primitives for an {@link IMessageRecipient}.
 * 
 * @author Gerrit
 * 
 */
public class MessageRecipientHelper {

	/**
	 * Registers the given {@link IMessageRecipient} as target for specific primitives with the given
	 * {@link IMessageHandlingAgent}. <br/>
	 * The primitives the {@link IMessageRecipient} is registered for are those that are defined by the most specialized
	 * class of type {@link Primitives} that can be found for the recipient.
	 * 
	 * @param recipient
	 *            the {@link IMessageRecipient} that is registered as target with the given
	 *            {@link IMessageHandlingAgent}.
	 * @param agent
	 *            the {@link IMessageHandlingAgent} that registers the primitives.
	 */
	public static void registerIMessageRecipientPrimitives(IMessageRecipient recipient, IMessageHandlingAgent agent) {
		Set<String> registeredPrimitives = agent.registerTargetForPrimitives(recipient);

		if (registeredPrimitives.isEmpty()) {
			// System.out.println("No primitives registered for recipient " + recipient + " of type " +
			// recipient.getClass());
		}
	}
}
