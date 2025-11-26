package andrew_volostnykh.webrunner.service.persistence;

import andrew_volostnykh.webrunner.DependenciesContainer;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;

public class NavigationTreePersistenceService {

	private static final Path STATE_FILE = Path.of("collections.json");

	@Getter @Setter
	private CollectionNode rootNode;

	public void save() {
		try {
			if (rootNode != null) {
				DependenciesContainer.getObjectMapper().writeValue(STATE_FILE.toFile(), rootNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CollectionNode load() {
		try {
			if (Files.exists(STATE_FILE)) {
				try {
					if (Files.exists(STATE_FILE)) {
						CollectionNode node = DependenciesContainer.getObjectMapper()
							.readValue(
								STATE_FILE.toFile(),
								CollectionNode.class
							);
						normalizeTree(node);
						return node;
					}
				} catch (Exception ignored) {}
			}
		} catch (Exception e) {
			System.err.println("Warning: collections storage file does not exist");
		}
		return null;
	}

	private void normalizeTree(CollectionNode node) {
		if (node.getRequest() != null) {
			node.setRequest(node.getRequest().deepCopy());
		}
		if (node.getChildren() != null) {
			node.getChildren().forEach(this::normalizeTree);
		}
	}
}
