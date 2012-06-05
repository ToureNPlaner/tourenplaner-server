/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.server;

import de.tourenplaner.database.DatabaseManager;
import de.tourenplaner.database.UserDataset;
import de.tourenplaner.database.UserStatusEnum;
import de.tourenplaner.utils.SHA1;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class Authorizer extends RequestHandler {

    private static Logger log = Logger.getLogger("de.tourenplaner.server");
    private static final Pattern COMPILE = Pattern.compile(" ");

    private final DatabaseManager dbm;


    public Authorizer(DatabaseManager dbm) {
        super(null);
        this.dbm = dbm;
    }

    /**
     * Generates a Salt, necessary for hash generation
     * @return The generated salt
     */
    protected String generateSalt() {
        // TODO optimize salt-generation
        final Random rand = new SecureRandom();
        final StringBuilder saltBuilder = new StringBuilder(64);
        for (int i = 0; i < 4; i++) {
            saltBuilder.append(Long.toHexString(rand.nextLong()));
        }

        return saltBuilder.toString();
    }

    /**
     * Authenticates a Request using HTTP Basic Authentication and returns the
     * UserDataset object of the authenticated user or null if authentication
     * failed. Errors will be sent to the client as error messages see protocol
     * specification for details. The connection will get closed after the error
     * has been sent. <br />
     * This method will check if the user is verified. If the user is not verified,
     * an error will be sent and the connection will get closed.
     *
     * @param myReq HttpRequest
     * @return the UserDataset object of the user or null if auth failed
     * @throws java.sql.SQLException Thrown if database query fails
     * @throws java.io.IOException Thrown if error message sending fails
     */
    public UserDataset auth(final HttpRequest myReq) throws SQLException, IOException {
        UserDataset user = authNoResponse(myReq);

        if (user == null) {
            responder.writeErrorMessage(ErrorMessage.EAUTH);
            return null;
        }

        if (user.status != UserStatusEnum.verified) {
            if (user.status == UserStatusEnum.needs_verification) {
                responder.writeErrorMessage(ErrorMessage.ENOTVERIFIED);
            } else {
                // for example if user is marked as deleted
                responder.writeErrorMessage(ErrorMessage.EAUTH);
            }
            return null;
        }

        return user;
    }

    /**
     * Authenticates a Request using HTTP Basic Authentication and returns the
     * UserDataset object of the authenticated user or null if authentication
     * failed. No error responses will be sent to the client. This method will
     * not check if the user is verified.
     *
     * @param myReq HttpRequest
     * @return the UserDataset object of the user or null if auth failed
     * @throws java.sql.SQLException Thrown if database query fails
     */
    public UserDataset authNoResponse(final HttpRequest myReq) throws SQLException {
        String email, emailandpw, pw;
        UserDataset user;
        int index;
        // Why between heaven and earth does Java have AES Encryption in
        // the standard library but not Base64 though it has it internally
        // several times
        emailandpw = myReq.getHeader("Authorization");
        if (emailandpw == null) {
            log.info("Missing Authorization header");
            return null;
        }
        // Basic Auth is: "realm BASE64OFPW"
        String[] parts = COMPILE.split(emailandpw);
        if(parts.length != 2){
            log.warning("Wrong Basic Auth Syntax");
            return null;
        }

        ChannelBuffer encodeddata;
        ChannelBuffer data;
        // Base64 is always ASCII
        encodeddata = ChannelBuffers.wrappedBuffer(parts[1].getBytes(CharsetUtil.US_ASCII));

        data = Base64.decode(encodeddata);
        // The string itself is utf-8
        emailandpw = data.toString(CharsetUtil.UTF_8);
        index = emailandpw.indexOf(':');
        if (index <= 0) {
            log.warning("Wrong Password Syntax in Basic Auth");
            return null;
        }

        email = emailandpw.substring(0, index);
        pw = emailandpw.substring(index + 1);
        user = dbm.getUser(email);

        if (user == null) {
            log.info("Wrong username");
            return null;
        }

        // Compute SHA1 of PW:SALT
        final String toHash = SHA1.SHA1(pw + ':' + user.salt);

        log.finer(pw + ':' + user.salt + " : " + toHash);
        if (!user.passwordhash.equals(toHash)) {
            log.info("Wrong username or password");
            return null;
        }

        return user;
    }
}
