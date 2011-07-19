/**
 * $$\\ToureNPlaner\\$$
 */

package server.threaded;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
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

import computecore.ComputeCore;
import computecore.ComputeRequest;

/**
 * @author Christoph Haag, Peter Vollmer
 *
 */
public class TourenPlanerRequestHandler implements HttpRequestHandler {

	private String username = null;
	private String password = null;

	private String algName;
	private final ComputeCore comCore;

	/** MessageDigest object used to compute SHA1 **/
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

			algName = URI.split("\\?")[0].substring(1);

			if (auth((HttpEntityEnclosingRequest) request)) {
				HttpServer
						.debugMsg(" RH: User Successfully Authenticated; Username: "
								+ username + "; Password: " + password);

				JSONObject requestJSON = null;
				/** JSONParser in here for threading **/
				final JSONParser parser = new JSONParser();
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

				// Create ComputeRequest and commit to workqueue
				final ComputeRequest req = new ComputeRequest(algName, objmap);
				// TODO: Something with comCore
				boolean sucess = comCore.submit(req);

				if (!sucess) {
					// TODO: server overload
					reply(generalError);
					return;
				}

				try {
					req.getWaitComputation().acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println(req.getResultObject().toString());

				EntityTemplate body = new EntityTemplate(new ContentProducer() {
					@Override
					public void writeTo(OutputStream outstream)
							throws IOException {
						OutputStreamWriter writer = new OutputStreamWriter(
								outstream, "UTF-8");
						writer.write(req.getResultObject().toString());
						writer.flush();
					}
				});
				body.setContentType("text/html; charset=UTF-8");
				response.setEntity(body);

				// EntityTemplate body = new EntityTemplate(defaultPage);
				// body.setContentType("text/html; charset=UTF-8");
				// response.setEntity(body);
			} else {
				// TODO
				reply(accessDenied);
				HttpServer
						.debugMsg(" RH: User authentication FAILED; Username: "
								+ username + "; Password: " + password);
				return;
			}
		}

		// a strange way to send a http response
		// EntityTemplate body = new EntityTemplate(defaultPage);
		// body.setContentType("text/html; charset=UTF-8");
		// response.setEntity(body);

	}

	public TourenPlanerRequestHandler(ComputeCore comCore) {
		// no idea why Object's constructor is called
		super();
		this.comCore = comCore;
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
	private boolean auth(HttpEntityEnclosingRequest request) {
		boolean authentic = false;

		String userandpw = request.getFirstHeader("Authorization").getValue();
		byte[] decoded = null;
		if (userandpw == null) {
			return false;
		}

		try {
			// http headers are supposed to be ascii
			decoded = userandpw.substring(userandpw.lastIndexOf(' ')).trim()
					.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		decoded = Base64.decodeBase64(decoded);
		// The string itself is utf-8
		try {
			userandpw = new String(decoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		userandpw = userandpw.trim();
		username = userandpw.split(":")[0];
		password = userandpw.split(":")[1];

		// TODO: real user and password
		if (userandpw.equals("FooUser:FooPassword")) {
			authentic = true;
		}
		System.out.println("Decoded String: " + userandpw);
		return authentic;
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
			writer.write("WWW-Authenticate: Basic realm=\"ToureNPlaner\"\n");
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
