import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

public class FileHandlerExample {

	static class HttpFileHandler implements HttpRequestHandler {

		private final String docRoot;

		public HttpFileHandler(final String docRoot) {
			super();
			this.docRoot = docRoot;
		}

		@Override
		public void handle(final HttpRequest request,
				final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {

			String method = request.getRequestLine().getMethod()
					.toUpperCase(Locale.ENGLISH);
			if (!method.equals("GET") && !method.equals("HEAD")
					&& !method.equals("POST")) {
				throw new MethodNotSupportedException(method
						+ " method not supported");
			}
			String target = request.getRequestLine().getUri();

			if (request instanceof HttpEntityEnclosingRequest) {
				HttpEntity entity = ((HttpEntityEnclosingRequest) request)
						.getEntity();
				byte[] entityContent = EntityUtils.toByteArray(entity);
				System.out.println("Incoming entity content (bytes): "
						+ entityContent.length);
			}

			final File file = new File(this.docRoot, URLDecoder.decode(target));
			if (!file.exists()) {

				response.setStatusCode(HttpStatus.SC_NOT_FOUND);
				EntityTemplate body = new EntityTemplate(new ContentProducer() {

					@Override
					public void writeTo(final OutputStream outstream)
							throws IOException {
						OutputStreamWriter writer = new OutputStreamWriter(
								outstream, "UTF-8");
						writer.write("<html><body><h1>");
						writer.write("File ");
						writer.write(file.getPath());
						writer.write(" not found");
						writer.write("</h1></body></html>");
						writer.flush();
					}

				});
				body.setContentType("text/html; charset=UTF-8");
				response.setEntity(body);
				System.out.println("File " + file.getPath() + " not found");

			} else if (!file.canRead() || file.isDirectory()) {

				response.setStatusCode(HttpStatus.SC_FORBIDDEN);
				EntityTemplate body = new EntityTemplate(new ContentProducer() {

					@Override
					public void writeTo(final OutputStream outstream)
							throws IOException {
						OutputStreamWriter writer = new OutputStreamWriter(
								outstream, "UTF-8");
						writer.write("<html><body><h1>");
						writer.write("Access denied");
						writer.write("</h1></body></html>");
						writer.flush();
					}

				});
				body.setContentType("text/html; charset=UTF-8");
				response.setEntity(body);
				System.out.println("Cannot read file " + file.getPath());

			} else {

				response.setStatusCode(HttpStatus.SC_OK);
				FileEntity body = new FileEntity(file, "text/html");
				response.setEntity(body);
				System.out.println("Serving file " + file.getPath());

			}
		}

	}
}
