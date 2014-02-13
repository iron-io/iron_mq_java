package io.iron.ironmq;

import java.io.IOException;

public interface IQueue {

	/**
	 * Retrieves a Message from the queue. If there are no items on the queue, an
	 * EmptyQueueException is thrown.
	 *
	 * @throws EmptyQueueException If the queue is empty.
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract Message get() throws IOException;

	/**
	 * Retrieves Messages from the queue. If there are no items on the queue, an
	 * EmptyQueueException is thrown.
	 * @param numberOfMessages The number of messages to receive. Max. is 100.
	 * @throws EmptyQueueException If the queue is empty.
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract Messages get(int numberOfMessages) throws IOException;

	/**
	 * Retrieves Messages from the queue. If there are no items on the queue, an
	 * EmptyQueueException is thrown.
	 * @param numberOfMessages The number of messages to receive. Max. is 100.
	 * @param timeout timeout in seconds.
	 * @throws EmptyQueueException If the queue is empty.
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract Messages get(int numberOfMessages, int timeout)
			throws IOException;

	/**
	 * Peeking at a queue returns the next messages on the queue, but it does not reserve them. 
	 * If there are no items on the queue, an EmptyQueueException is thrown.
	 *
	 * @throws EmptyQueueException If the queue is empty.
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract Message peek() throws IOException;

	/**
	 * Peeking at a queue returns the next messages on the queue, but it does not reserve them.
	 * 
	 * @param numberOfMessages The number of messages to receive. Max. is 100.
	 * @throws EmptyQueueException If the queue is empty.
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract Messages peek(int numberOfMessages) throws IOException;

	/**
	 * Touching a reserved message extends its timeout to the duration specified when the message was created.
	 *
	 * @param id The ID of the message to delete.
	 *
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract void touchMessage(String id) throws IOException;

	/**
	 * Deletes a Message from the queue.
	 *
	 * @param id The ID of the message to delete.
	 *
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract void deleteMessage(String id) throws IOException;

	/**
	 * Destroy the queue.
	 * 
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract void destroy() throws IOException;

	/**
	 * Deletes a Message from the queue.
	 *
	 * @param msg The message to delete.
	 *
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract void deleteMessage(Message msg) throws IOException;

	/**
	 * Pushes a message onto the queue.
	 *
	 * @param msg The body of the message to push.
	 * @return The new message's ID
	 *
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract String push(String msg) throws IOException;

	/**
	 * Pushes a message onto the queue.
	 *
	 * @param msg The body of the message to push.
	 * @param timeout The message's timeout in seconds.
	 * @return The new message's ID
	 *
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract String push(String msg, long timeout) throws IOException;

	/**
	 * Pushes a message onto the queue.
	 *
	 * @param msg The body of the message to push.
	 * @param timeout The message's timeout in seconds.
	 * @param delay The message's delay in seconds.
	 * @return The new message's ID
	 *
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract String push(String msg, long timeout, long delay)
			throws IOException;

	/**
	 * Pushes a message onto the queue.
	 *
	 * @param msg The body of the message to push.
	 * @param timeout The message's timeout in seconds.
	 * @param delay The message's delay in seconds.
	 * @param expiresIn The message's expiration offset in seconds.
	 * @return The new message's ID
	 *
	 * @throws HTTPException If the IronMQ service returns a status other than 200 OK.
	 * @throws IOException If there is an error accessing the IronMQ server.
	 */
	public abstract String push(String msg, long timeout, long delay,
			long expiresIn) throws IOException;

	/**
	 * Clears the queue off all messages
	 * @throws IOException
	 */
	public abstract void clear() throws IOException;

	/**
	 * @return the name of this queue
	 */
	public abstract String getName();

	public abstract int getSize() throws IOException;

}