/*
 * Copyright 2012 Robert 'Bobby' Zenz. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Robert 'Bobby' Zenz ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Robert 'Bobby' Zenz OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Robert 'Bobby' Zenz.
 */
package org.bonsaimind.easyminelauncher;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

public class Main {

	private static final String name = "EasyMineLauncher";
	private static final String version = "0.15";

	public static void main(String[] args) {
		String jarDir = "";
		String jar = "";
		String lwjglDir = "";
		String password = "";
		String nativeDir = "";
		List<String> additionalJars = new ArrayList<String>();
		boolean noFrame = false;
		String optionsFileFrom = "";
		List<String> options = new ArrayList<String>();
		boolean demo = false;
		String parentDir = "";
		String port = "25565";
		String server = null;
		boolean authenticate = false;
		AuthenticationFailureBehavior authenticationFailureBehavior = AuthenticationFailureBehavior.ALERT_BREAK;
		int keepAliveTick = 300;
		String sessionId = "0";
		String launcherVersion = "381";
		String authenticationAddress = "https://login.minecraft.net";
		String username = "Username";
		boolean useLastLogin = false;
		boolean saveLastLogin = false;
		boolean keepUsername = false;
		String texturepack = "";
		String title = "Minecraft (" + name + ")";
		boolean maximized = false;
		int width = 800;
		int height = 600;
		int x = -1;
		int y = -1;
		boolean alwaysOnTop = false;
		boolean fullscreen = false;
		float opacity = 1f;
		boolean dump = false;
		boolean noExit = false;

		// Parse arguments
		for (String arg : args) {
			if (arg.startsWith("--jar-dir=")) {
				jarDir = arg.substring(10);
			} else if (arg.startsWith("--jar=")) {
				jar = arg.substring(6);
			} else if (arg.startsWith("--lwjgl-dir=")) {
				lwjglDir = arg.substring(12);
			} else if (arg.startsWith("--mppass=")) {
				password = arg.substring(9);
			} else if (arg.startsWith("--password=")) {
				password = arg.substring(11);
			} else if (arg.startsWith("--native-dir=")) {
				nativeDir = arg.substring(13);
			} else if (arg.startsWith("--additional-jar=")) {
				String param = arg.substring(17);
				additionalJars.addAll(Arrays.asList(param.split(",")));
			} else if (arg.equals("--no-frame")) {
				noFrame = true;
			} else if (arg.startsWith("--parent-dir=")) {
				parentDir = arg.substring(13);
			} else if (arg.startsWith("--port=")) {
				port = arg.substring(7);
			} else if (arg.startsWith("--server=")) {
				server = arg.substring(9);
			} else if (arg.equals("--authenticate")) {
				authenticate = true;
			} else if (arg.startsWith("--authentication-failure=")) {
				authenticationFailureBehavior = AuthenticationFailureBehavior.valueOf(arg.substring(25));
			} else if (arg.startsWith("--keep-alive-tick=")) {
				keepAliveTick = Integer.parseInt(arg.substring(18));
			} else if (arg.startsWith("--session-id=")) {
				sessionId = arg.substring(13);
			} else if (arg.startsWith("--launcher-version=")) {
				launcherVersion = arg.substring(19);
			} else if (arg.startsWith("--auth-address=")) {
				authenticationAddress = arg.substring(15);
			} else if (arg.startsWith("--options-file=")) {
				optionsFileFrom = arg.substring(15);
			} else if (arg.startsWith("--set-option=")) {
				options.add(arg.substring(13));
			} else if (arg.startsWith("--texturepack=")) {
				texturepack = arg.substring(14);
			} else if (arg.startsWith("--title=")) {
				title = arg.substring(8);
			} else if (arg.startsWith("--username=")) {
				username = arg.substring(11);
			} else if (arg.equals("--use-lastlogin")) {
				useLastLogin = true;
			} else if (arg.equals("--save-lastlogin")) {
				saveLastLogin = true;
			} else if (arg.equals("--keep-username")) {
				keepUsername = true;
			} else if (arg.equals("--demo")) {
				demo = true;
			} else if (arg.equals("--version")) {
				printVersion();
				return;
			} else if (arg.startsWith("--width=")) {
				width = Integer.parseInt(arg.substring(8));
			} else if (arg.startsWith("--height=")) {
				height = Integer.parseInt(arg.substring(9));
			} else if (arg.startsWith("--x=")) {
				x = Integer.parseInt(arg.substring(4));
			} else if (arg.startsWith("--y=")) {
				y = Integer.parseInt(arg.substring(4));
			} else if (arg.equals("--maximized")) {
				maximized = true;
			} else if (arg.equals("--always-on-top")) {
				alwaysOnTop = true;
			} else if (arg.equals("--fullscreen")) {
				fullscreen = true;
			} else if (arg.startsWith("--opacity=")) {
				opacity = Float.parseFloat(arg.substring(10));
			} else if (arg.equals("--dump")) {
				dump = true;
			} else if (arg.equals("--no-exit")) {
				noExit = true;
			} else if (arg.equals("--help")) {
				printHelp();
				return;
			} else {
				System.err.println("Unknown parameter: " + arg);
				printHelp();
				return;
			}
		}

		// Check the arguments
		if (jarDir.isEmpty() && jar.isEmpty()) {
			jarDir = new File(new File(System.getProperty("user.home"), ".minecraft").toString(), "bin").toString();
		}

		if (jarDir.isEmpty()) {
			jarDir = new File(jar).getParent();
		} else {
			jarDir = new File(jarDir).getAbsolutePath();
			jar = jarDir;
		}
		if (lwjglDir.isEmpty()) {
			lwjglDir = jarDir;
		}
		if (nativeDir.isEmpty()) {
			nativeDir = new File(jarDir, "natives").getAbsolutePath();
		}
		if (!parentDir.isEmpty()) {
			System.setProperty("user.home", parentDir);

			// This is needed for the Forge ModLoader and maybe others.
			System.setProperty("minecraft.applet.TargetDirectory", parentDir);
		} else {
			parentDir = System.getProperty("user.home");
		}
		parentDir = new File(parentDir, ".minecraft").toString();

		// Shall we read from the lastlogin file?
		if (useLastLogin) {
			String[] lastLogin = getLastLogin(new File(parentDir));

			if (lastLogin == null || lastLogin.length <= 1) {
				System.err.println("Failed to read from the last login file!");
			} else {
				username = lastLogin[0];
				password = lastLogin[1];
			}
		}

		if (dump) {
			System.out.println("jarDir (exists: " + new File(jarDir).exists() + "): " + jarDir);
			System.out.println("jar (exists: " + new File(jar).exists() + "): " + jar);
			System.out.println("lwjglDir (exists: " + new File(lwjglDir).exists() + "): " + lwjglDir);
			System.out.println("password: " + password);
			System.out.println("nativeDir (exists: " + new File(nativeDir).exists() + "): " + nativeDir);
			System.out.println("additionalJars:");
			for (String additionalJar : additionalJars) {
				System.out.println("    " + additionalJar);
			}
			System.out.println("noFrame: " + noFrame);
			System.out.println("optionsFileFrom (exists: " + new File(optionsFileFrom).exists() + "): " + optionsFileFrom);
			System.out.println("options:");
			for (String option : options) {
				System.out.println("    " + option);
			}
			System.out.println("demo: " + demo);
			System.out.println("parentDir (exists: " + new File(parentDir).exists() + "): " + parentDir);
			System.out.println("port: " + port);
			System.out.println("server: " + server);
			System.out.println("authenticate: " + authenticate);
			System.out.println("authenticationFailureBehavior: " + authenticationFailureBehavior);
			System.out.println("keepAliveTick: " + keepAliveTick);
			System.out.println("launcherVersion: " + launcherVersion);
			System.out.println("authenticationAddress: " + authenticationAddress);
			System.out.println("username: " + username);
			System.out.println("useLastLogin: " + useLastLogin);
			System.out.println("saveLastLogin: " + saveLastLogin);
			System.out.println("keepUsername: " + keepUsername);
			System.out.println("texturepack: " + texturepack);
			System.out.println("maximized: " + maximized);
			System.out.println("width: " + width);
			System.out.println("height: " + height);
			System.out.println("x: " + x);
			System.out.println("y: " + y);
			System.out.println("title: " + title);
			System.out.println("alwaysOnTop: " + alwaysOnTop);
			System.out.println("fullscreen: " + fullscreen);
			System.out.println("opacity: " + opacity);
			return;
		}

		// Now try if we manage to login...
		if (authenticate) {
			try {
				AuthenticationResult result = authenticate(authenticationAddress, username, password, launcherVersion);
				sessionId = result.getSessionId();

				// Only launch the keep alive ticker if the login was successfull.
				if (keepAliveTick > 0) {
					Timer timer = new Timer("Authentication Keep Alive", true);
					final String finalUsername = username;
					final String finalSessionId = sessionId;
					final String finalAuthenticationAddress = authenticationAddress;
					timer.scheduleAtFixedRate(new TimerTask() {

						@Override
						public void run() {
							System.out.println("Authentication Keep Alive.");
							try {
								keepAlive(finalAuthenticationAddress, finalUsername, finalSessionId);
							} catch (AuthenticationException ex) {
								System.err.println("Authentication-Keep-Alive failed!");
								System.err.println(ex);
							}
						}
					}, keepAliveTick * 1000, keepAliveTick * 1000);
				}

				if (saveLastLogin) {
					setLastlogin(new File(parentDir), username, password);
				}

				if (!keepUsername) {
					username = result.getUsername();
				}
			} catch (AuthenticationException ex) {
				System.err.println(ex);
				if (ex.getCause() != null) {
					System.err.println(ex.getCause());
				}

				// Alert the user
				if (authenticationFailureBehavior == AuthenticationFailureBehavior.ALERT_BREAK
						|| authenticationFailureBehavior == AuthenticationFailureBehavior.ALERT_CONTINUE) {
					JOptionPane.showMessageDialog(new JInternalFrame(), ex.getMessage(), "Failed to authenticate...", JOptionPane.ERROR_MESSAGE);
				}
				// STOP!
				if (authenticationFailureBehavior == AuthenticationFailureBehavior.ALERT_BREAK
						|| authenticationFailureBehavior == AuthenticationFailureBehavior.SILENT_BREAK) {
					return;
				}
			}
		}

		// Let's work with the options.txt, shall we?
		OptionsFile optionsFile = new OptionsFile(parentDir);
		if (!optionsFileFrom.isEmpty()) {
			optionsFile.setPath(optionsFileFrom);
		}

		if (optionsFile.exists() && optionsFile.read()) {
			// Reset the path in case we used an external options.txt.
			optionsFile.setPath(parentDir);
		} else {
			System.out.println("Failed to read options.txt from \"" + optionsFile.getPath() + "\" or it does not exist!");
		}

		// Set the texturepack.
		if (!texturepack.isEmpty() && optionsFile.isRead()) {
			optionsFile.setTexturePack(texturepack);
		}

		// Set the options.
		if (!options.isEmpty() && optionsFile.isRead()) {
			for (String option : options) {
				int splitIdx = option.indexOf(":");
				if (splitIdx > 0) { // we don't want not-named options either.
					optionsFile.setOption(option.substring(0, splitIdx), option.substring(splitIdx + 1));
				}
			}
		}

		// Now write back.
		if (optionsFile.isRead()) {
			if (!optionsFile.write()) {
				System.out.println("Failed to write options.txt!");
			}
		}


		// Some checks.
		if (height <= 0) {
			height = 600;
		}
		if (width <= 0) {
			width = 800;
		}

		// Load the launcher
		if (!additionalJars.isEmpty()) {
			try {
				// This might fix issues for Mods which assume that they
				// are loaded via the real launcher...not sure, thought adding
				// it would be a good idea.
				List<URL> urls = new ArrayList<URL>();
				for (String item : additionalJars) {
					urls.add(new File(item).toURI().toURL());
				}
				if (!extendClassLoaders(urls.toArray(new URL[urls.size() - 1]))) {
					System.err.println("Failed to inject additional jars!");
					return;
				}
			} catch (MalformedURLException ex) {
				System.err.println("Failed to load additional jars!");
				System.err.println(ex);
				return;
			}

		}

		// Let's tell the Forge ModLoader (and others) that it is supposed
		// to load our applet and not that of the official launcher.
		System.setProperty("minecraft.applet.WrapperClass", "org.bonsaimind.easyminelauncher.ContainerApplet");

		// Create the applet.
		ContainerApplet container = new ContainerApplet();

		// Pass arguments to the applet.
		container.setDemo(demo);
		container.setUsername(username);
		if (server != null) {
			container.setServer(server, port);
		}
		container.setMpPass(password);
		container.setSessionId(sessionId);

		// Create and set up the frame.
		ContainerFrame frame = new ContainerFrame(title);

		if (fullscreen) {
			Dimension dimensions = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setAlwaysOnTop(true);
			frame.setUndecorated(true);
			frame.setSize(dimensions.width, dimensions.height);
			frame.setLocation(0, 0);
		} else {
			frame.setAlwaysOnTop(alwaysOnTop);
			frame.setUndecorated(noFrame);
			frame.setSize(width, height);

			// It is more likely that no location is set...I think.
			frame.setLocation(
					x == -1 ? frame.getX() : x,
					y == -1 ? frame.getY() : y);
			if (maximized) {
				frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			}
		}

		if (opacity < 1) {
			frame.setUndecorated(true);
			frame.setOpacity(opacity);
		}

		frame.setContainerApplet(container);
		frame.setVisible(true);

		// Load
		container.loadNatives(nativeDir);
		if (container.loadJarsAndApplet(jar, lwjglDir)) {
			container.init();
			container.start();
		} else {
			System.err.println("Failed to load Minecraft! Exiting.");

			if (noExit) {
				return;
			} else {
				// Exit just to be sure.
				System.exit(0);
			}
		}
	}

	private static AuthenticationResult authenticate(String address, String username, String password, String launcherVersion) throws AuthenticationException {
		try {
			username = URLEncoder.encode(username, "UTF-8");
			password = URLEncoder.encode(password, "UTF-8");
			launcherVersion = URLEncoder.encode(launcherVersion, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new AuthenticationException("Failed to encode username, password or launcher version!", ex);
		}

		String request = String.format("user=%s&password=%s&version=%s", username, password, launcherVersion);
		String response = httpRequest(address, request);
		String[] splitted = response.split(":");

		if (splitted.length < 5) {
			throw new AuthenticationException(response);
		}

		return new AuthenticationResult(splitted);
	}

	/**
	 * This is mostly from here: http://stackoverflow.com/questions/252893/how-do-you-change-the-classpath-within-java
	 * @param url
	 * @return
	 */
	private static boolean extendClassLoaders(URL[] urls) {
		// Extend the ClassLoader of the current thread.
		URLClassLoader loader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(loader);

		// Extend the SystemClassLoader...this is needed for mods which will
		// use the WhatEver.getClass().getClassLoader() method to retrieve
		// a ClassLoader.
		URLClassLoader systemLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

		try {
			Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
			addURLMethod.setAccessible(true);

			for (URL url : urls) {
				addURLMethod.invoke(systemLoader, url);
			}

			return true;
		} catch (NoSuchMethodException ex) {
			System.err.println(ex);
		} catch (SecurityException ex) {
			System.err.println(ex);
		} catch (IllegalAccessException ex) {
			System.err.println(ex);
		} catch (InvocationTargetException ex) {
			System.err.println(ex);
		}

		return false;
	}

	/**
	 * Reads and decrypts the content of the lastlogin file.
	 * @param from The lastlogin-file or the directory to which to read from.
	 * @return A 2-dimensional String array which consists of username [0] and password [1].
	 */
	private static String[] getLastLogin(File from) {
		if (from.isDirectory()) {
			from = new File(from.getAbsolutePath(), "lastlogin");
		}
		from = from.getAbsoluteFile();

		if (!from.exists()) {
			System.err.println("File does not exist: " + from.getAbsolutePath());
			return null;
		}

		try {
			DataInputStream stream = new DataInputStream(new CipherInputStream(new FileInputStream(from), getLastLoginCipher(Cipher.DECRYPT_MODE)));
			return new String[]{stream.readUTF(), stream.readUTF()};
		} catch (FileNotFoundException ex) {
			System.err.println(ex);
			return null;
		} catch (IOException ex) {
			System.err.println(ex);
			return null;
		}
	}

	/**
	 * Initializes a cipher which can be used to decrypt the lastlogin file...or encrypt, that is.
	 * @param cipherMode
	 * @return
	 */
	private static Cipher getLastLoginCipher(int cipherMode) {
		byte[] salt = {
			(byte) 0x0c, (byte) 0x9d, (byte) 0x4a, (byte) 0xe4,
			(byte) 0x1e, (byte) 0x83, (byte) 0x15, (byte) 0xfc
		};
		String password = "passwordfile";

		try {
			PBEParameterSpec parameter = new PBEParameterSpec(salt, 5);
			SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
			Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
			cipher.init(cipherMode, key, parameter);
			return cipher;
		} catch (NoSuchAlgorithmException ex) {
			System.err.println(ex);
			return null;
		} catch (InvalidKeySpecException ex) {
			System.err.println(ex);
			return null;
		} catch (NoSuchPaddingException ex) {
			System.err.println(ex);
			return null;
		} catch (InvalidKeyException ex) {
			System.err.println(ex);
			return null;
		} catch (InvalidAlgorithmParameterException ex) {
			System.err.println(ex);
			return null;
		}
	}

	private static String httpRequest(String url, String content) throws AuthenticationException {
		byte[] contentBytes = null;
		try {
			contentBytes = content.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new AuthenticationException("Failed to convert content!", ex);
		}

		URLConnection connection = null;
		try {
			connection = new URL(url).openConnection();
		} catch (MalformedURLException ex) {
			throw new AuthenticationException("It wasn't me!", ex);
		} catch (IOException ex) {
			throw new AuthenticationException("Failed to connect to authentication server!", ex);
		}
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", Integer.toString(contentBytes.length));

		try {
			OutputStream requestStream = connection.getOutputStream();
			requestStream.write(contentBytes, 0, contentBytes.length);
			requestStream.close();
		} catch (IOException ex) {
			throw new AuthenticationException("Failed to write request!", ex);
		}

		String response = "";

		try {
			BufferedReader responseStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			response = responseStream.readLine();
			responseStream.close();
		} catch (IOException ex) {
			throw new AuthenticationException("Failed to read response!", ex);
		}

		return response;
	}

	private static void keepAlive(String address, String username, String sessionId) throws AuthenticationException {
		httpRequest(address, String.format("?name={0}&session={1}", username, sessionId));
	}

	private static void printVersion() {
		System.out.println(name + " " + version);
		System.out.println("Copyright 2012 Robert 'Bobby' Zenz. All rights reserved.");
		System.out.println("Licensed under 2-clause-BSD.");
	}

	private static void printHelp() {
		System.out.println("Usage: " + name + ".jar [OPTIONS]");
		System.out.println("Launch Minecraft directly.");
		System.out.println("");

		InputStream stream = Main.class.getResourceAsStream("/org/bonsaimind/easyminelauncher/help.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			reader.close();
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	private static void setLastlogin(File to, String username, String password) {
		if (to.isDirectory()) {
			to = new File(to.getAbsolutePath(), "lastlogin");
		}
		to = to.getAbsoluteFile();

		if (!to.exists()) {
			try {
				to.createNewFile();
			} catch (IOException ex) {
				System.err.println(ex);
				return;
			}
		}

		try {
			DataOutputStream stream = new DataOutputStream(new CipherOutputStream(new FileOutputStream(to), getLastLoginCipher(Cipher.ENCRYPT_MODE)));
			stream.writeUTF(username);
			stream.writeUTF(password);
			stream.close();
		} catch (FileNotFoundException ex) {
			System.err.println(ex);
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
}
