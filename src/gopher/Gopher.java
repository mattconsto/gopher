package gopher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
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
		
		for(char c : new char[] {'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'g', 'h', 'I', 's', 'T'}) {
			iconMap.put(c, new Image(getClass().getResourceAsStream("/icons/" + c + ".png")));
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

    		TextArea textArea = new TextArea();
    		textArea.setStyle("-fx-font-family: \"Courier New\";");
    		
    		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
    			GopherMenuEntry entry = newValue.getValue();
            	if(entry.type == '1') {
            		try {
						addGopherMenu(entry.uri, newValue);
						newValue.setExpanded(true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	} else {
            		Socket socket;
					try {
						socket = new Socket(entry.uri.getHost(), entry.uri.getPort());
	            		DataOutputStream out    = new DataOutputStream(socket.getOutputStream());
	            		BufferedReader   in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
            	}
            });
    		
    		addGopherMenu(uri, treeRoot);
    		
    		SplitPane splitPane = new SplitPane(treeView, textArea);
    		Tab tab = new Tab(uri.host, splitPane);
    		
    		gopherTabs.getTabs().add(tab);
    		gopherTabs.getSelectionModel().select(gopherTabs.getTabs().size() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void addGopherMenu(RegexURI uri, TreeItem<GopherMenuEntry> root) throws UnknownHostException, IOException {
		Socket           socket = new Socket(uri.getHost(), uri.getPort());
		DataOutputStream out    = new DataOutputStream(socket.getOutputStream());
		BufferedReader   in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		System.out.println(uri);
		
		out.writeBytes(uri.getPath() + "\r\n");
		out.flush();

		while(true) {
			String line = in.readLine();
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
