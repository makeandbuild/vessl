package com.makeandbuild.vessl.persistence.couch;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class Base64BasicAuthEncoder implements Encoder<String> {
    @Override
    public String encode(String... input) {
        if (input.length < 2) throw new IllegalArgumentException("Must provide a username and password");
        try {
            BASE64Encoder encoder = new BASE64Encoder();
            return "Basic " + encoder.encode((input[0] + ":" + input[1]).getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Could not encode values for basic authentication", e);
        }
    }
}
