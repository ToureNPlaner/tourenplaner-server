package de.tourenplaner.computecore;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is an interface you can implement to be able to
 * write objects of that as results via ComputeRequests
 */
public interface StreamJsonWriter {
    void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException;
}
