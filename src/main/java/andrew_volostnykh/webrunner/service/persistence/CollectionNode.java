package andrew_volostnykh.webrunner.service.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionNode {
	private String name;
	private boolean isFolder;
	// isChain
	private List<CollectionNode> children = new ArrayList<>();
	private RequestDefinition request;

	public void addChild(CollectionNode node) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(node);
	}

	@Override
	public String toString() {
		return name;
	}
}
