import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TourenPlanerRequestHandler implements HttpRequestHandler {

	private String username = null;
	private String signature = null;
	private String query = null;
	private String algName;
	private Map<String, String> map = null;

	/** JSONParser we can reuse **/
	private final JSONParser parser = new JSONParser();
	/** MessageDigest object used to compute SHA1 **/
	private MessageDigest digester;
	private HttpResponse response;

	@Override
	public void handle(final HttpRequest request, final HttpResponse response,
			final HttpContext context) throws HttpException, IOException {
		this.response = response;
		HttpServer.debugMsg(" RH: RH: TourenPlaner Requesthandler called");

		String method = request.getRequestLine().getMethod()
				.toUpperCase(Locale.ENGLISH);
		if (!method.equals("GET") && !method.equals("HEAD")
				&& !method.equals("POST")) {
			throw new MethodNotSupportedException(method
					+ " method not supported");
		}

		request.getAllHeaders()[0].getValue();
		String URI = request.getRequestLine().getUri();
		HttpServer.debugMsg(" RH: Received request with URI: " + URI);

		// no idea what this does
		if (request instanceof HttpEntityEnclosingRequest) {
			HttpEntity entity = ((HttpEntityEnclosingRequest) request)
					.getEntity();
			byte[] entityContent = EntityUtils.toByteArray(entity);
			HttpServer.debugMsg(" RH: Incoming entity content (bytes): "
					+ entityContent.length + " of type "
					+ entity.getContentType());

			try {
				query = (new URI(URI)).getQuery();
			} catch (URISyntaxException e) {
				System.err.println("Failed to parse URI " + URI);
				return;
			}

			map = getQueryMap(query);
			algName = URI.split("\\?")[0].substring(1);

			HttpServer.debugMsg(" RH: query: " + query);
			for (String key : map.keySet()) {
				if (key.equalsIgnoreCase("tp-user")) {
					username = map.get(key);
					HttpServer.debugMsg(" RH: username: " + username);
				} else if (key.equalsIgnoreCase("tp-signature")) {
					signature = map.get(key);
					HttpServer.debugMsg(" RH: signature: " + signature);
				}
			}

			if (username == null || signature == null) {
				reply(generalError);
				return;
			}

			if (auth(username, signature, entityContent)) {
				HttpServer.debugMsg(" RH: User " + username
						+ " Successfully Authenticated");

				JSONObject requestJSON = null;
				try {
					// TODO: avoid useless casting
					requestJSON = (JSONObject) parser.parse(new String(
							entityContent));
				} catch (ParseException e) {
					HttpServer.errorLog("JSON Parse error: " + e.getMessage());
					e.printStackTrace();
				}
				HttpServer.debugMsg("requested JSON: "
						+ requestJSON.toJSONString());
				Map<String, Object> objmap = requestJSON;

				HttpServer.debugMsg("Algname: " + algName);

				// TODO: start computation here
				boolean sucess = false; // did the calculation of the problemm
										// succeed?
				if (!sucess) {
					// TODO: server overload
					reply(generalError);
				}

				// EntityTemplate body = new EntityTemplate(defaultPage);
				// body.setContentType("text/html; charset=UTF-8");
				// response.setEntity(body);
			} else {
				// TODO
				reply(accessDenied);
				HttpServer.debugMsg(" RH: User " + username
						+ " authentication FAILED");
				return;
			}
		}

		// a strange way to send a http response
		// EntityTemplate body = new EntityTemplate(defaultPage);
		// body.setContentType("text/html; charset=UTF-8");
		// response.setEntity(body);

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
	private boolean auth(String username, String signature, byte[] content) {

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
			// HttpServer.debugMsg(" RH: User: "+username.get(0));
			// HttpServer.debugMsg(" RH: Signature: "+signature.get(0));
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
				HttpServer.debugMsg(" RH: Bodyhash: " + sb.toString());
				sb.append(':');
				try {
					sb.append(realSecret);
					// Now sb is BODYHASH:SECRET do the final hashing and hex
					// conversion
					digester.reset();
					digester.update(sb.toString().getBytes("UTF-8"));
					finalhash = digester.digest();
					// Reset the string builder so we can reuse it
					// HttpServer.debugMsg(" RH: Before Hashing: "+sb.toString());
					sb.delete(0, sb.length());
					for (byte element : finalhash) {
						sb.append(Character.forDigit((element >> 4) & 0xf, 16));
						sb.append(Character.forDigit(element & 0xf, 16));
					}
					// HttpServer.debugMsg(" RH: MyHash: "+sb.toString());
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

	private void reply(ContentProducer cp) {
		EntityTemplate body = new EntityTemplate(cp);
		body.setContentType("text/html; charset=UTF-8");
		response.setEntity(body);
	}

	ContentProducer defaultPage = new ContentProducer() {
		@Override
		public void writeTo(final OutputStream outstream) throws IOException {
			OutputStreamWriter writer = new OutputStreamWriter(outstream,
					"UTF-8");
			writer.write("<html><body><h1>");
			writer.write("This is a http response");
			writer.write("</h1></body></html>");
			writer.flush();
		}
	};

	ContentProducer accessDenied = new ContentProducer() {
		// HTTP/1.1 401 Access Denied
		// WWW-Authenticate: Basic realm="My Server"
		// Content-Length: 0
		@Override
		public void writeTo(final OutputStream outstream) throws IOException {
			OutputStreamWriter writer = new OutputStreamWriter(outstream,
					"UTF-8");
			writer.write("HTTP/1.1 401 Access Denied\n");
			writer.write("WWW-Authenticate: Basic realm=\"gerbera.informatik.uni-stutgart.de\"\n");
			writer.write("Content-Length: 0\n");
			writer.flush();
		}
	};

	ContentProducer generalError = new ContentProducer() {
		@Override
		public void writeTo(final OutputStream outstream) throws IOException {
			OutputStreamWriter writer = new OutputStreamWriter(outstream,
					"UTF-8");
			writer.write("<html><body><h1>");
			writer.write("An error happened");
			writer.write("</h1></body></html>");
			writer.flush();
		}
	};

}
