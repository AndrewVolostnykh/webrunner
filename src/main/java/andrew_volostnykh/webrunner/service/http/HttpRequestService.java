package andrew_volostnykh.webrunner.service.http;

import andrew_volostnykh.webrunner.service.AbstractService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class HttpRequestService
	extends AbstractService {

	public static final List<String> HTTP_METHODS = List.of(
		"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
	);

	public HttpResponseData sendRequest(
		String method,
		String url,
		String body,
		Map<String, String> headers
	) throws Exception {
		try (HttpClient client = HttpClient.newHttpClient()) {

			HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(
					URI.create(
						normalizeUrl(url)
					)
				);

			headers.forEach(builder::header);

			switch (method) {
				case "POST" -> builder.POST(BodyPublishers.ofString(body));
				case "PUT" -> builder.PUT(BodyPublishers.ofString(body));
				case "DELETE" -> builder.DELETE();
				default -> builder.GET();
			}

			// TODO: set headers etc separately in HttpResponseData
			HttpResponse<String> response = client.send(
				builder.build(),
				HttpResponse.BodyHandlers.ofString()
			);

			return new HttpResponseData(
				response.statusCode(),
				response.headers().map(),
				response.body()
			);
		}
	}

	private static String normalizeUrl(String url) {
		if (!url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
			return "http://" + url;
		}
		return url;
	}
}
