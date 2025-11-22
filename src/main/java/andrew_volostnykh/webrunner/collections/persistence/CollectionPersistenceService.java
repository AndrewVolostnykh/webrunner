package andrew_volostnykh.webrunner.collections.persistence;

import andrew_volostnykh.webrunner.collections.CollectionNode;
import andrew_volostnykh.webrunner.collections.RequestDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;

public class CollectionPersistenceService {

	private static final Path STATE_FILE = Path.of("collections.json");
	private final ObjectMapper mapper = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT);

	@Getter @Setter
	private CollectionNode rootNode;

	public void save() {
		try {
			if (rootNode != null) {
				mapper.writeValue(STATE_FILE.toFile(), rootNode);
				System.out.println("✔ Collections saved.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CollectionNode load() {
		try {
			if (Files.exists(STATE_FILE)) {
				System.out.println("Collections loaded.");
				return mapper.readValue(STATE_FILE.toFile(), CollectionNode.class);
			}
		} catch (Exception e) {
			System.err.println("Warning: collections storage file does not exist");
		}
		return null;
	}
}
