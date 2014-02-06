package serverPackage;

/**
 * WebServer class runs the web server which allows users to connect to the login web page.
 * It also starts the AppHomeServer running.
 */

import static spark.Spark.*;
import spark.*;

public class WebServer {

	private static boolean authenticated = false;
	private static ServerProtocol proto;
	private static AppHomeServer ahs;

	public static void main(String[] args) {

		ahs = new AppHomeServer();
		startAHS sAHS = new startAHS(ahs);
		Thread t = new Thread(sAHS);
		t.start();

		// First Spark get method returns a simple webpage with two text entry
		// boxes: one for username and one for password.
		get(new Route("/hello") {
			@Override
			public Object handle(Request request, Response response) {

				response.type("text/html");

				return ""
						+ "<form name=\"input\" action=\"goto\" method=\"post\">"
						+ "Username: <input type=\"text\" name=\"user\">"
						+ "<br>"
						+ "Password: <input type=\"password\" name=\"pwd\">"
						+ "<br>" + "<input type=\"submit\" value=\"Submit\">"
						+ "</form>";
			}

		});

		// Post method that takes the submitted response from the username and
		// password, calling the authenticate method
		// to ensure the correct values have been entered. Throws a halt error
		// if incorrect combination or user declined login, redirects to the
		// goto confirmation page if correct values entered.

		post(new Route("/goto") {
			@Override
			public Object handle(Request request, Response response) {

				String username = request.queryMap().get("user").value();
				String pass = request.queryMap().get("pwd").value();
				proto = new ServerProtocol(ahs);
				authenticated = proto.authenticate(username, pass);
				if (!authenticated) {
					halt(401, "You are not welcome here");
				}
				return "Login successful. Welcome!";
			}
		});

	}
}

/**
 * Additional thread class that starts the AppHomeServer running at the
 * simultaneously with the WebServer so that users can connect to web pages and
 * devices can connect to the AppHomeServer at the same time.
 */
class startAHS implements Runnable {

	public AppHomeServer ahs;

	public startAHS(AppHomeServer ahs) {
		this.ahs = ahs;
	}

	public void run() {
		ahs.startServer();
	}
}