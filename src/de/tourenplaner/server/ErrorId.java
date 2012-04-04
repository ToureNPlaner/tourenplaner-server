package de.tourenplaner.server;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 *  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public enum ErrorId {
    EUNKNOWNALG ("An unknown algorithm was requested", HttpResponseStatus.NOT_FOUND),
    EBUSY ("This server is currently too busy to fulfill the request", HttpResponseStatus.SERVICE_UNAVAILABLE),
    EBADJSON ("Could not parse supplied JSON", HttpResponseStatus.BAD_REQUEST),
    EAUTH ("Wrong username or password", HttpResponseStatus.UNAUTHORIZED),
    ENOTVERIFIED ("User account is not verified", HttpResponseStatus.FORBIDDEN),
    EUNKNOWNURL ("An unknown URL was requested", HttpResponseStatus.NOT_FOUND),
    EDATABASE ("The server can't contact its database", HttpResponseStatus.INTERNAL_SERVER_ERROR),
    ELIMIT ("The given limit is invalid", HttpResponseStatus.BAD_REQUEST),
    EOFFSET ("The given offset is invalid", HttpResponseStatus.BAD_REQUEST),
    ENOUSERID ("The given user id is unknown to this server", HttpResponseStatus.NOT_FOUND),
    ENOREQUESTID ("The given request id is unknown to this server", HttpResponseStatus.NOT_FOUND),
    ENOTADMIN ("You are not an admin", HttpResponseStatus.FORBIDDEN),
    EREGISTERED ("This email is already registered", HttpResponseStatus.FORBIDDEN),
    ECOMPUTE ("", HttpResponseStatus.INTERNAL_SERVER_ERROR);
    
    public final String errorId;
    public final String message;
    public final HttpResponseStatus status;
    
    ErrorId(String message, HttpResponseStatus status) {
        this.errorId = this.name();
        this.message = message;
        this.status = status;
    }
}
