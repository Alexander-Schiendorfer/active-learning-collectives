package masconcepts.agent;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * This interface should be implement by a class that represents an agent in a common multi-agent system (MAS). The
 * agent has an identifier and clearly defined interface used for communication with other agents within the system.
 * 
 * @author gerrit
 * 
 */
public interface IMessageHandlingAgent extends Serializable, IAgent, IMessageRecipient {

	/**
	 * Waits <code>timeout</code> ms for a reply to a message with identifier <code>messageIdentifier</code> and
	 * primitive <code>thePrimitive</code> from an agent with identifier <code>agentIdentifier</code>.If the reply is
	 * not received within the specified time, a {@link TimeoutException} is thrown.<br/>
	 * The reply should response to a message with the specified <code>messageIdentifier</code>.
	 * 
	 * @param messageIdentifier
	 *            the identifier of the message the reply should response to
	 * @param agentIdentifier
	 *            the identifier of the message's originator
	 * @param thePrimitive
	 *            the primitive that determines the type of message
	 * @param timeout
	 *            the time waited for a reply before a {@link TimeoutException} is thrown
	 * @return The data assigned to the primitive.
	 * @throws TimeoutException
	 *             thrown if a timeout while waiting for the reply occurred
	 * @throws RuntimeException
	 *             thrown if an exception was thrown while processing a message that should trigger the reply
	 */
	public Serializable waitForReply(UUID messageIdentifier, String agentIdentifier, String thePrimitive, int timeout) throws TimeoutException,
			RuntimeException;

	/**
	 * Sends a message with primitive <code>thePrimitive</code> and payload <code>data</code> to an agent with
	 * identifier <code>agentIdentifier</code>. Afterwards, the method waits <code>timeout</code> ms for a reply with
	 * primitive <code>"return" + thePrimitive</code> from an agent with identifier <code>agentIdentifier</code>.
	 * 
	 * @param agentIdentifier
	 *            the identifier of the message recipient
	 * @param thePrimitive
	 *            the primitive that determines the type of message
	 * @param timeout
	 *            the time waited for a reply before a {@link TimeoutException} is thrown
	 * @param data
	 *            the parameters of the massage to process the request
	 * @return The data passed in the reply.
	 * @throws TimeoutException
	 *             thrown if a timeout while waiting for the reply occurred
	 * @throws RuntimeException
	 *             thrown if an exception was thrown while processing the message
	 */
	public Serializable sendRequestAndWaitForReply(String agentIdentifier, String thePrimitive, int timeout, Serializable... data) throws TimeoutException,
			RuntimeException;

	/**
	 * Sends a message with primitive <code>thePrimitive</code> and payload <code>data</code> to an agent with
	 * identifier <code>agentIdentifier</code>.
	 * 
	 * @param agentIdentifier
	 *            the identifier of the message recipient
	 * @param thePrimitive
	 *            the primitive that determines the type of message
	 * @param data
	 *            the parameters of the massage to process the request
	 * @return the message's unique identifier if the message could be sent, or <code>null</code> if the message could
	 *         not be sent.
	 */
	public UUID sendRequest(String agentIdentifier, String thePrimitive, Serializable... data);

	/**
	 * Sends a message with primitive <code>thePrimitive</code> and payload <code>data</code> to an agent with
	 * identifier <code>agentIdentifier</code>.
	 * 
	 * @param agentIdentifier
	 *            the identifier of the message recipient
	 * @param thePrimitive
	 *            the primitive that determines the type of message
	 * @param data
	 *            the parameters of the massage to process the event
	 * @return the message's unique identifier if the message could be sent, or <code>null</code> if the message could
	 *         not be sent.
	 */
	public UUID sendEvent(String agentIdentifier, String thePrimitive, Serializable... data);

	/**
	 * Sends a broadcast message with primitive <code>thePrimitive</code> and payload <code>data</code> to all agents
	 * with the specific type of <code>typeOfReceiver</code>. Afterwards the method waits and waits <code>timeout</code>
	 * ms for a reply.
	 * 
	 * @param typeOfReceiver
	 *            the type of the message recipient
	 * @param thePrimitive
	 *            the primitive that determines the type of message
	 * @param timeout
	 *            the time waited for a reply before a {@link TimeoutException} is thrown
	 * @param data
	 *            the parameters of the massage to process the request
	 * @return The data passed in the reply.
	 * @throws TimeoutException
	 *             thrown if a timeout while waiting for the reply occurred
	 * @throws RuntimeException
	 *             thrown if an exception was thrown while processing the message
	 */
	public Serializable broadcastRequestAndWaitForReply(String typeOfReceiver, String thePrimitive, int timeout, Serializable... data) throws TimeoutException,
			RuntimeException;

	/**
	 * Sends a broadcast message with primitive <code>thePrimitive</code> and payload <code>data</code> to all agents
	 * with the specific type <code>typeOfReceiver</code>.
	 * 
	 * @param typeOfReceiver
	 *            the type of the message recipient
	 * @param thePrimitive
	 *            the primitive that determines the type of message
	 * @param data
	 *            the parameters of the massage to process the request
	 * @return the message's unique identifier if the message could be sent, or <code>null</code> if the message could
	 *         not be sent
	 */
	public UUID broadcastRequest(String typeOfReceiver, String thePrimitive, Serializable... data);

	/**
	 * Sends a broadcast message with primitive <code>thePrimitive</code> and payload <code>data</code> to all agents
	 * with specific type <code>typeOfReceiver</code>.
	 * 
	 * @param typeOfReceiver
	 *            the type of the message recipient
	 * @param thePrimitive
	 *            the primitive that determines the type of message
	 * @param data
	 *            the parameters of the massage to process the event
	 * @return the message's unique identifier if the message could be sent, or <code>null</code> if the message could
	 *         not be sent
	 */
	public UUID broadcastEvent(String typeOfReceiver, String thePrimitive, Serializable... data);

	/**
	 * Registers an {@link IMessageRecipient} as target for messages with primitives equal to the string values of the
	 * fields of type {@link String} defined in the {@link Primitives} class of the {@link IMessageRecipient} or its
	 * superclasses. This means that methods associated with registered primitives are called on the given
	 * {@link IMessageRecipient} whenever a corresponding message is received and processed.
	 * 
	 * @param recipient
	 *            the object that is to be registered as target for primitives stated by the values of the fields of the
	 *            {@link Primitives} class of {@link IMessageRecipient} or its superclasses.
	 * 
	 * @return a set of registered primitives
	 */
	public Set<String> registerTargetForPrimitives(IMessageRecipient recipient);
}
