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

package de.tourenplaner.utils;

import org.jboss.netty.util.CharsetUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class SHA1 {

    private static String convertToHex(byte[] data) {
        final StringBuilder hexbuilder = new StringBuilder(data.length * 2);
        for (byte b : data) {
            hexbuilder.append(Integer.toHexString((b >>> 4) & 0x0F));
            hexbuilder.append(Integer.toHexString(b & 0x0F));
        }
        return hexbuilder.toString();
    }

    public static String SHA1(String text)  {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(text.getBytes(CharsetUtil.UTF_8), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
}