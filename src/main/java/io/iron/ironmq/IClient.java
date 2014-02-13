package io.iron.ironmq;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public interface IClient {

	/**
	 * Returns a Queue using the given name.
	 * The network is not accessed during this call.
	 *
	 * @param name The name of the Queue to create.
	 */
	public abstract IQueue queue(String name);

	public abstract Map<String, Object> getOptions();

	public abstract Object getOption(String name);

	public abstract String getEnv();

	Reader delete(String endpoint) throws IOException;

    Reader get(String endpoint) throws IOException;

    Reader post(String endpoint, String body) throws IOException;
}