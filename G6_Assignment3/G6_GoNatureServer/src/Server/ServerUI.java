package Server;

import javafx.application.Application;
import javafx.stage.Stage;
import GUI.ServerInterface;

public class ServerUI extends Application {

    // static reference so the GUI controller can attach the log callback
    public static EchoServer serverInstance = null;

    public static void main(String args[]) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ServerInterface portFrame = new ServerInterface();
        portFrame.start(primaryStage);
    }

    public static void runServer(String p) {
        int port = 0;
        try {
            port = Integer.parseInt(p);
        } catch (Exception e) {
            System.out.println("GoNatureServer: invalid port number.");
            e.printStackTrace();
        }
        serverInstance = new EchoServer(port);
        try {
            serverInstance.listen();
        } catch (Exception ex) {
            System.out.println("GoNatureServer: failed to start listening.");
            ex.printStackTrace();
        }
    }
}