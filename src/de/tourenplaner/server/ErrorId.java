package de.tourenplaner.server;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 *  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public enum ErrorId {

    EAUTH (
            "Wrong username or password",
            HttpResponseStatus.UNAUTHORIZED),

    EBADJSON (
            "Could not parse supplied JSON",
            HttpResponseStatus.BAD_REQUEST),

    EBADJSON_NOCONTENT (
            EBADJSON,
            "Content is empty"),

    EBADJSON_INCORRECT_USER_OBJ (
            EBADJSON,
            "JSON user object was not correct (needs email, password, firstname, lastname, address)"),

    EBADJSON_INCORRECT_USER_OBJ_ADMIN (
            EBADJSON,
            "JSON user object was not correct (\"admin\" should be boolean)"),

    EBADJSON_INCORRECT_USER_OBJ_STATUS (
            EBADJSON,
            "JSON user object was not correct (\"status\" was not a valid value)"),

    EBUSY (
            "This server is currently too busy to fulfill the request",
            HttpResponseStatus.SERVICE_UNAVAILABLE),

    ECOMPUTE (
            HttpResponseStatus.INTERNAL_SERVER_ERROR),

    ECOMPUTE_RESULT_NOT_SENT_OR_STORED (
            "The server could not send and not store the compute result",
            HttpResponseStatus.INTERNAL_SERVER_ERROR),

    EDATABASE (
            "The server can't contact its database",
            HttpResponseStatus.INTERNAL_SERVER_ERROR),

    ELIMIT (
            "The parameter limit is missing or invalid",
            HttpResponseStatus.BAD_REQUEST),

    ELIMIT_MISSING (
            ELIMIT,
            "The parameter limit is missing"),

    ELIMIT_NOT_NAT_NUMBER(
            ELIMIT,
            "The given parameter limit is not a natural number"),

    ENOREQUESTID (
            "The given request id is unknown to this server",
            HttpResponseStatus.NOT_FOUND),

    ENOUSERID (
            "The given user id is unknown to this server",
            HttpResponseStatus.NOT_FOUND),

    ENOTADMIN (
            "You are not an admin",
            HttpResponseStatus.FORBIDDEN),

    ENOTVERIFIED (
            "User account is not verified",
            HttpResponseStatus.FORBIDDEN),

    EOFFSET (
            "The parameter offset is missing or invalid",
            HttpResponseStatus.BAD_REQUEST),

    EOFFSET_MISSING (
            EOFFSET,
            "The parameter offset is missing"),

    EOFFSET_NOT_NAT_NUMBER(
            EOFFSET,
            "The given parameter offset is not a natural number"),

    EREGISTERED (
            "This email is already registered",
            HttpResponseStatus.FORBIDDEN),

    EUNKNOWNALG (
            "An unknown algorithm was requested",
            HttpResponseStatus.NOT_FOUND),

    EUNKNOWNURL (
            "An unknown URL was requested",
            HttpResponseStatus.NOT_FOUND);
    
    public final String errorId;
    public final String message;
    public final String detail;
    public final HttpResponseStatus status;

    ErrorId(HttpResponseStatus status) {
        this("", status);
    }
    
    ErrorId(String message, HttpResponseStatus status) {
        this(message, "", status);
    }

    ErrorId(String message, String detail, HttpResponseStatus status) {
        this.errorId = this.name();
        this.message = message;
        this.detail = detail;
        this.status = status;
    }

    ErrorId(ErrorId errorId, String detail) {
        this(errorId.message, detail, errorId.status);
    }

}
