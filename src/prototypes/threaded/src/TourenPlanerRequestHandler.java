import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;

public class TourenPlanerRequestHandler implements HttpRequestHandler {
	/** JSONParser we can reuse **/
	private final JSONParser parser = new JSONParser();
	/** MessageDigest object used to compute SHA1 **/
	private MessageDigest digester;

	@Override
	public void handle(final HttpRequest request, final HttpResponse response,
			final HttpContext context) throws HttpException, IOException {

		System.out.println("TourenPlaner Requesthandler called");

		String method = request.getRequestLine().getMethod()
				.toUpperCase(Locale.ENGLISH);
		if (!method.equals("GET") && !method.equals("HEAD")
				&& !method.equals("POST")) {
			throw new MethodNotSupportedException(method
					+ " method not supported");
		}

		String URI = request.getRequestLine().getUri();
		System.out.println("Received request with URI: " + URI);

		// no idea what this does
		if (request instanceof HttpEntityEnclosingRequest) {
			HttpEntity entity = ((HttpEntityEnclosingRequest) request)
					.getEntity();
			byte[] entityContent = EntityUtils.toByteArray(entity);
			System.out.println("Incoming entity content (bytes): "
					+ entityContent.length + " of type "
					+ entity.getContentType());
			// copied and modified from nick
			/*
			 * List<NameValuePair> params = null; try { params =
			 * URLEncodedUtils.parse(new URI(URI), "UTF-8"); } catch
			 * (URISyntaxException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 * 
			 * // get values from url String username = null; // TODO: passwort?
			 * // String password; String hash = null; for (NameValuePair nvp :
			 * params) { if (nvp.getName().equalsIgnoreCase("tp-user")) {
			 * username = nvp.getValue(); System.out.println("Username: " +
			 * username); } else if
			 * (nvp.getName().equalsIgnoreCase("tp-signature")) { hash =
			 * nvp.getValue(); System.out.println("Hash: " + hash); } }
			 */

			String username = null;
			// TODO: passwort?
			// String password = null;
			String signature = null;
			String query = null;
			try {
				query = (new URI(URI)).getQuery();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("query: " + query);
			Map<String, String> map = getQueryMap(query);
			for (String key : map.keySet()) {
				if (key.equalsIgnoreCase("tp-user")) {
					username = (String) map.get(key);
					System.out.println("username: " + username);
				} else if (key.equalsIgnoreCase("tp-signature")) {
					signature = map.get(key);
					System.out.println("signature: " + signature);
				}
			}

			if (username == null || signature == null) {
				// TODO: send error
				return;
			}
			
			ByteArrayBuffer content = null;
		
			if (auth(username, signature, entityContent)) {
				System.out.println("User " + username + " Successfully Authenticated");
			} else
			{
				//TODO: Error page
				System.out.println("User " + username + " authentication FAILED");
			}
		}

		// a strange way to send a http response
		EntityTemplate body = new EntityTemplate(new ContentProducer() {
			@Override
			public void writeTo(final OutputStream outstream)
					throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(outstream,
						"UTF-8");
				writer.write("<html><body><h1>");
				writer.write("This is a http response");
				writer.write("</h1></body></html>");
				writer.flush();
			}
		});
		body.setContentType("text/html; charset=UTF-8");
		response.setEntity(body);

	}

	public TourenPlanerRequestHandler() {
		// no idea why Object's constructor is called
		super();
	}

	/**
	 * Authenticats the request in the ChannelBuffer content with the parameters
	 * given in params see: @link
	 * https://gerbera.informatik.uni-stuttgart.de/projects
	 * /server/wiki/Authentifizierung for a detailed explanation
	 * 
	 * @param params
	 * @param content
	 * @return
	 */
	private boolean auth(String username, String signature,
			byte[] content) {
		
    	try {
			digester = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Could not load SHA1 algorithm");
			System.exit(1);
		}
		
		boolean authentic = false;
		if (true) {
			String realSecret;
			byte[] bodyhash, finalhash;

			// System.out.println("User: "+username.get(0));
			// System.out.println("Signature: "+signature.get(0));
			// TODO replace static test with database access
			if (username.equals("FooUser")) {
				realSecret = "FooPassword";
				// Hash the content
				digester.reset();

				digester.update(content, 0, content.length);

				bodyhash = digester.digest();
				// Contenthash to String and then hash for SHA1(BODYHASH:SECRET)
				StringBuilder sb = new StringBuilder();
				/**
				 * @link 
				 *       http://www.spiration.co.uk/post/1199/Java-md5-example-with
				 *       -MessageDigest
				 **/
				for (byte element : bodyhash) {
					sb.append(Character.forDigit((element >> 4) & 0xf, 16));
					sb.append(Character.forDigit(element & 0xf, 16));
				}
				// DEBUG
				System.out.println("Bodyhash: " + sb.toString());
				sb.append(':');
				try {
					sb.append(realSecret);
					// Now sb is BODYHASH:SECRET do the final hashing and hex
					// conversion
					digester.reset();
					digester.update(sb.toString().getBytes("UTF-8"));
					finalhash = digester.digest();
					// Reset the string builder so we can reuse it
					// System.out.println("Before Hashing: "+sb.toString());
					sb.delete(0, sb.length());
					for (byte element : finalhash) {
						sb.append(Character.forDigit((element >> 4) & 0xf, 16));
						sb.append(Character.forDigit(element & 0xf, 16));
					}
					// System.out.println("MyHash: "+sb.toString());
					authentic = (sb.toString().equals(signature)) ? true
							: false;
				} catch (UnsupportedEncodingException e) {
					System.err
							.println("We can't fcking find UTF-8 Charset hell wtf?");
				}

			}

		}

		return authentic;
	}

	public static Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}
}
