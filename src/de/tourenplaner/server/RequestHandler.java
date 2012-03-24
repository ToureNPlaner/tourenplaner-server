package de.tourenplaner.server;

/**
 * User: Niklas Schnelle
 * Date: 12/27/11
 * Time: 12:59 AM
 */
public abstract class RequestHandler {

    protected Responder responder;

    protected RequestHandler(Responder responder) {
        this.responder = responder;
    }

    /**
     * Sets the Responder to use, this must be called before
     * messages can be handled
     *
     * @param responder
     */
    public void setResponder(Responder responder) {
        this.responder = responder;
    }
}
