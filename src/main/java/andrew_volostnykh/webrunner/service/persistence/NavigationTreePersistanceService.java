package andrew_volostnykh.webrunner.service.persistence;

import andrew_volostnykh.webrunner.DependenciesContainer;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;

public class NavigationTreePersistanceService {

	private static final Path STATE_FILE = Path.of("collections.json");

	@Getter @Setter
	private CollectionNode rootNode;

	public void save() {
		try {
			if (rootNode != null) {
				DependenciesContainer.getObjectMapper().writeValue(STATE_FILE.toFile(), rootNode);
				System.out.println("Collections saved");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CollectionNode load() {
		try {
			if (Files.exists(STATE_FILE)) {
				System.out.println("Collections loaded");
				return DependenciesContainer.getObjectMapper().readValue(STATE_FILE.toFile(), CollectionNode.class);
			}
		} catch (Exception e) {
			System.err.println("Warning: collections storage file does not exist");
		}
		return null;
	}
}
