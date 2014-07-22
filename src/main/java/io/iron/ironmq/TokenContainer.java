package io.iron.ironmq;

import java.io.IOException;
import java.net.MalformedURLException;

public interface TokenContainer {
    String getToken() throws IOException;
}
