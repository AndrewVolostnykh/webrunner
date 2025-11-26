package andrew_volostnykh.webrunner.service.persistence;

import andrew_volostnykh.webrunner.DependenciesContainer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class NavigationTreeService {

	// should it be really static?
	public static final NavigationTreePersistenceService NAVIGATION_TREE_PERSISTENCE_SERVICE =
		DependenciesContainer.collectionPersistenceService();

	public static TreeItem<CollectionNode> initRootItem() {
		CollectionNode saved = NAVIGATION_TREE_PERSISTENCE_SERVICE.load();
		TreeItem<CollectionNode> rootItem;

		if (saved != null) {
			rootItem = buildTreeItem(saved);
			rootItem.setExpanded(true);
			NAVIGATION_TREE_PERSISTENCE_SERVICE.setRootNode(saved);
		} else {
			CollectionNode rootNode = new CollectionNode("Requests", true, null, null);
			rootItem = new TreeItem<>(rootNode);
			rootItem.setExpanded(true);
			NAVIGATION_TREE_PERSISTENCE_SERVICE.setRootNode(rootNode);
		}

		return rootItem;
	}

	private static TreeItem<CollectionNode> buildTreeItem(CollectionNode node) {
		TreeItem<CollectionNode> item = new TreeItem<>(node);
		if (node.getChildren() != null) {
			node.getChildren().forEach(child -> item.getChildren().add(buildTreeItem(child)));
		}
		return item;
	}

	public static void addContextMenus(
		TreeView<CollectionNode> collectionTree,
		Consumer<RequestDefinition> loadRequest
	) {
		collectionTree.setCellFactory(treeView -> new TreeCell<>() {
			private final FontIcon folderIcon = new FontIcon("mdi-folder:16");
			private final FontIcon requestIcon = new FontIcon("mdi-web:16");

			private final ContextMenu folderMenu = new ContextMenu();
			private final MenuItem addRequestItem = new MenuItem("Add Request");
			private final MenuItem addFolderItem = new MenuItem("Add Folder");
			private final MenuItem deleteFolderItem = new MenuItem("Delete Folder");

			private final ContextMenu requestMenu = new ContextMenu();
			private final MenuItem deleteRequestItem = new MenuItem("Delete Request");

			{
				addRequestItem.setOnAction(e -> createRequestInsideNode(
					getItem(),
					getTreeItem(),
					collectionTree,
					loadRequest
				));
				addFolderItem.setOnAction(e -> createFolderInsideNode(
					getItem(),
					getTreeItem()
				));
				deleteFolderItem.setOnAction(e -> deleteNode(getItem(), getTreeItem()));
				folderMenu.getItems().addAll(addRequestItem, addFolderItem, deleteFolderItem);

				deleteRequestItem.setOnAction(e -> deleteNode(getItem(), getTreeItem()));
				requestMenu.getItems().add(deleteRequestItem);
			}

			@Override
			protected void updateItem(
				CollectionNode item,
				boolean empty
			) {
				super.updateItem(item, empty);

				if (empty || item == null) {
					setText(null);
					setGraphic(null);
					setContextMenu(null);
				} else {
					setText(item.getName());
					setGraphic(item.isFolder() ? folderIcon : requestIcon);

					setContextMenu(item.isFolder() ? folderMenu : requestMenu);
				}
			}
		});
	}

	private static void deleteNode(
		CollectionNode node,
		TreeItem<CollectionNode> treeItem
	) {
		TreeItem<CollectionNode> parent = treeItem.getParent();
		if (parent != null) {
			parent.getChildren().remove(treeItem);
			parent.getValue().getChildren().remove(node);
			NAVIGATION_TREE_PERSISTENCE_SERVICE.save();
		}
	}

	private static void createFolderInsideNode(
		CollectionNode folderNode,
		TreeItem<CollectionNode> folderItem
	) {
		TextInputDialog dialog = new TextInputDialog("New Folder");
		dialog.setTitle("Create Folder");
		dialog.setHeaderText("Enter folder name:");
		dialog.showAndWait().ifPresent(name -> {
			if (!name.isBlank()) {
				CollectionNode newFolder = new CollectionNode(name, true, new ArrayList<>(), null);
				folderNode.addChild(newFolder);

				TreeItem<CollectionNode> newItem = new TreeItem<>(newFolder);
				folderItem.getChildren().add(newItem);

				NAVIGATION_TREE_PERSISTENCE_SERVICE.save();
			}
		});
	}

	private static void createRequestInsideNode(
		CollectionNode folderNode,
		TreeItem<CollectionNode> folderItem,
		TreeView<CollectionNode> collectionTree,
		Consumer<RequestDefinition> loadRequest
	) {
		TextInputDialog dialog = new TextInputDialog("New Request");
		dialog.setTitle("Create Request");
		dialog.setHeaderText("Create new HTTP Request");
		dialog.setContentText("Enter request name:");

		dialog.showAndWait().ifPresent(name -> {
			if (!name.isBlank()) {
				// TODO: add hints
				RequestDefinition request = new RequestDefinition(
					UUID.randomUUID().toString(),
					name,
					"GET",
					"",
					new HashMap<>(),
					"",
					"",
					"",
					RequestType.HTTP_REQUEST
				);

				CollectionNode newNode = new CollectionNode(
					name,
					false,
					null,
					request
				);

				System.err.println("FOLDER: " + folderNode.getRequest());

				folderNode.addChild(newNode);
				TreeItem<CollectionNode> newItem = new TreeItem<>(newNode);
				folderItem.getChildren().add(newItem);

				NAVIGATION_TREE_PERSISTENCE_SERVICE.save();
				collectionTree.getSelectionModel().select(newItem);
				loadRequest.accept(request);
			}
		});
	}

	public static void addListenerOnCreate(
		TreeView<CollectionNode> collectionTree,
		Consumer<RequestDefinition> loadRequest
	) {
		collectionTree.getSelectionModel()
			.selectedItemProperty()
			.addListener((obs, oldVal, newVal) -> {
				if (newVal != null && !newVal.getValue().isFolder()) {
					loadRequest.accept(newVal.getValue().getRequest());
				}
			});
	}
}
