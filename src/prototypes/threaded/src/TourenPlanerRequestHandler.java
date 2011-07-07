import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

public class TourenPlanerRequestHandler implements HttpRequestHandler {

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
			List<NameValuePair> params = null;
			try {
				params = URLEncodedUtils.parse(new URI(URI), "UTF-8");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// get values from url
			String username = null;
			// TODO: passwort?
			// String password;
			String hash = null;
			for (NameValuePair nvp : params) {
				if (nvp.getName().equalsIgnoreCase("tp-user")) {
					username = nvp.getValue();
					System.out.println("Username: " + username);
				} else if (nvp.getName().equalsIgnoreCase("tp-signature")) {
					hash = nvp.getValue();
					System.out.println("Hash: " + hash);
				}
			}
			if (username == null || hash == null) {
				// TODO: send error
				return;
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

}
