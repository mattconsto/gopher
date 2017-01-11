package gopher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Gopher extends Application {
	@FXML private TextField gopherAddress;
	@FXML private Button	gopherAdd;
	@FXML private TabPane   gopherTabs;

	protected RegexURI defaultAddress = new RegexURI("gopher", null, null, null, 70, "/", null, null);

	protected Pattern gopherMenuPattern = Pattern.compile("^(?<type>\\w)(?<message>.*)\\t(?<path>[^\\t]*)\\t(?<host>[^\\t]+)\\t(?<port>\\d+)$");

	protected Map<Character, Image> iconMap = new HashMap<>();

	@Override public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Gopher");
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gopher.fxml"));
		loader.setController(this);
		primaryStage.setScene(new Scene((Pane) loader.load()));
		primaryStage.show();

		for(char c : new char[] {'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'd', 'g', 'h', 'I', 'p', 's', 'T'}) {
			InputStream icon = getClass().getResourceAsStream("/icons/" + c + ".png");
			if(icon != null) {
				iconMap.put(c, new Image(icon));
			} else {
				System.err.println(c + " icon not found!");
			}
		}

		gopherAddress.textProperty().addListener((ObservableValue<? extends String> observable,
			String oldValue, String newValue) -> {
				if(newValue != null && !newValue.equals(oldValue)) {
					try {
						RegexURI uri = defaultAddress.apply(new RegexURI(newValue));
						if(uri.getScheme().equals("gopher")) gopherAdd.setDisable(false);
					} catch (URISyntaxException e) {
						gopherAdd.setDisable(true);
						System.err.println(e);
					}
				}
				
		});

	}

	@FXML
	private void addGopher(ActionEvent event) {
		try {
			RegexURI uri = defaultAddress.apply(new RegexURI(gopherAddress.getText()));
			gopherAddress.setText(uri.toString());

			TreeItem<GopherMenuEntry> treeRoot = new TreeItem<GopherMenuEntry>(new GopherMenuEntry('1', uri.getHost(), uri));
			treeRoot.setExpanded(true);
			TreeView<GopherMenuEntry> treeView = new TreeView<>(treeRoot);
			treeView.setShowRoot(false);
			treeView.setStyle("-fx-font-family: \"Courier New\";");

			BorderPane contentArea = new BorderPane();
			SplitPane splitPane = new SplitPane(treeView, contentArea);
			Tab tab = new Tab(uri.host, splitPane);
			
			gopherTabs.getTabs().add(tab);
			gopherTabs.getSelectionModel().select(gopherTabs.getTabs().size() - 1);
			
			treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
				if(newValue == null) return;

				GopherMenuEntry entry = newValue.getValue();
				
				System.out.println(entry.uri);
				
				switch(entry.type) {
					case '0': { // PlainText
						Socket socket;
						try {
							TextArea textArea = new TextArea();
							textArea.setStyle("-fx-font-family: \"Courier New\";");
							contentArea.centerProperty().set(textArea);

							socket = new Socket(entry.uri.getHost(), entry.uri.getPort());
							DataOutputStream out	= new DataOutputStream(socket.getOutputStream());
							BufferedReader   in	 = new BufferedReader(new InputStreamReader(socket.getInputStream()));

							gopherAddress.setText(entry.uri.toString());

							out.writeBytes(entry.uri.getPath() + "\r\n");
							out.flush();

							textArea.setText("");
							while(true) {
								String line = in.readLine();
								if(line == null) break;
								textArea.setText(textArea.getText() + line + "\n");
							}

							socket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} break;
					case '1': { // DirectoryListing
						try {
							if(uri.getHost().equals(entry.uri.getHost())) {
								addGopherMenu(entry.uri, newValue);
								newValue.setExpanded(true);
							} else {
								gopherAddress.setText(entry.uri.toString());
								addGopher(new ActionEvent());
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					} break;
					case '2': { // SearchQuery
					} break;
					case '3': { // ErrorMessage
						Platform.runLater(() -> treeView.getSelectionModel().select(oldValue));
					} break;
					case '4': { // BinHexFile
					} break;
					case '5': { // BinaryArchive
					} break;
					case '6': { // UUEncodedText
					}break;
					case '7': { // SearchEngineQuery
						TextInputDialog dialog = new TextInputDialog();
						dialog.setTitle("Search Query");
						dialog.setHeaderText("Please enter your query");

						// Traditional way to get the response value.
						Optional<String> result = dialog.showAndWait();
						if (result.isPresent()){
							Socket socket;
							try {
								TextArea textArea = new TextArea();
								textArea.setStyle("-fx-font-family: \"Courier New\";");
								contentArea.centerProperty().set(textArea);
	
								socket = new Socket(entry.uri.getHost(), entry.uri.getPort());
								DataOutputStream out	= new DataOutputStream(socket.getOutputStream());
								BufferedReader   in	 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	
								gopherAddress.setText(entry.uri.toString());
	
								out.writeBytes(entry.uri.getPath() + "\t" + result.get() + "\r\n");
								out.flush();
	
								textArea.setText("");
								while(true) {
									String line = in.readLine();
									if(line == null) break;
									textArea.setText(textArea.getText() + line + "\n");
								}
	
								socket.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					break;
					case '8': { // TelnetSessionPointer
					} break;
					case 's': // wav audio
					case 'd': // pdf
					case '9': { // BinaryFile
						FileChooser fileChooser = new FileChooser();
						String[] path = entry.uri.getPath().split("/");
						fileChooser.setInitialFileName(path[path.length - 1]);
						File file = fileChooser.showSaveDialog(null);
						
						if (file != null) {
							Socket socket;
							
							try {
								socket = new Socket(entry.uri.getHost(), entry.uri.getPort());
								DataOutputStream out = new DataOutputStream(socket.getOutputStream());
								
								out.writeBytes(entry.uri.getPath() + "\r\n");
								out.flush();
								
								Files.copy(socket.getInputStream(), file.toPath());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					break;
					case 'h': { // HTMLFile
						WebView browser = new WebView();
						WebEngine webEngine = browser.getEngine();
						webEngine.load(entry.uri.getPath().replaceFirst("URL:", ""));

						contentArea.centerProperty().set(browser);
					} break;
					case 'i': { // Information
						Platform.runLater(() -> treeView.getSelectionModel().select(oldValue));
					} break;
					case 'g': // GIF
					case 'p': // PNG
					case 'I': { // JPEGImage
						Socket socket;
						
						try {
							socket = new Socket(entry.uri.getHost(), entry.uri.getPort());
							DataOutputStream out = new DataOutputStream(socket.getOutputStream());
							
							out.writeBytes(entry.uri.getPath() + "\r\n");
							out.flush();
							
							ImageView image = new WrappedImageView(new Image(socket.getInputStream()));
							contentArea.centerProperty().set(new ScrollPane(image));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} break;
					case 'S': { // TN3270SessionPointer
					} break;
					default: {
						// Do nothing!
					}
				}});

			addGopherMenu(uri, treeRoot);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void addGopherMenu(RegexURI uri, TreeItem<GopherMenuEntry> root) throws UnknownHostException, IOException {
		Socket		     socket = new Socket(uri.getHost(), uri.getPort());
		DataOutputStream out    = new DataOutputStream(socket.getOutputStream());
		BufferedReader   in	    = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		System.out.println(uri);

		out.writeBytes(uri.getPath() + "\r\n");
		out.flush();

		while(true) {
			String line = in.readLine();
			System.out.println(line);
			if(line == null) break;

			Matcher matcher = gopherMenuPattern.matcher(line);

			if(matcher.matches()) {
				ImageView imageView = new ImageView(iconMap.get(iconMap.containsKey(matcher.group("type").charAt(0)) ? matcher.group("type").charAt(0) : 'z'));
				imageView.setPreserveRatio(true);
				imageView.setFitHeight(16);
				TreeItem<GopherMenuEntry> treeItem = new TreeItem<GopherMenuEntry>(new GopherMenuEntry(matcher.group("type").charAt(0), matcher.group("message"), uri.setHost(matcher.group("host")).setPort(Integer.parseInt(matcher.group("port"))).setPath(matcher.group("path"))), imageView);
				root.getChildren().add(treeItem);
			} else {
				root.getChildren().add(new TreeItem<GopherMenuEntry>(new GopherMenuEntry('z', "", null)));
			}
		}


		socket.close();
	}
}
