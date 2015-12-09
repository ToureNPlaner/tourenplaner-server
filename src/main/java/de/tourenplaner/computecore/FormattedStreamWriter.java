package de.tourenplaner.computecore;


import de.tourenplaner.computeserver.Responder;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is an interface you can implement to be able to
 * write objects of that as results via ComputeRequests
 */
public interface FormattedStreamWriter {
    void writeToStream(Responder.ResultFormat format, OutputStream stream) throws IOException;
}
