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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import org.bonsaimind.minecraftmiddleknife.Authentication;
import org.bonsaimind.minecraftmiddleknife.AuthenticationResponse;
import org.bonsaimind.minecraftmiddleknife.Blender;
import org.bonsaimind.minecraftmiddleknife.ClassLoaderExtender;
import org.bonsaimind.minecraftmiddleknife.LastLogin;
import org.bonsaimind.minecraftmiddleknife.LastLoginCipherException;
import org.bonsaimind.minecraftmiddleknife.OptionsFile;

public class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	private static final String NAME = "EasyMineLauncher";
	private static final String VERSION = "0.16.1";

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
		String parentDir = System.getProperty("user.home");
		String appletToLoad = "net.minecraft.client.MinecraftApplet";
		List<String> blendWith = new ArrayList<String>();
		String blendJarName = "minecraft_blended.jar";
		boolean blendKeepManifest = false;
		String port = "25565";
		String server = null;
		boolean authenticate = false;
		AuthenticationFailureBehavior authenticationFailureBehavior = AuthenticationFailureBehavior.ALERT_BREAK;
		int keepAliveTick = 300;
		String sessionId = "0";
		String launcherVersion = Authentication.LAUNCHER_VERSION;
		String authenticationAddress = Authentication.MOJANG_SERVER;
		String username = "Username";
		boolean useLastLogin = false;
		boolean saveLastLogin = false;
		boolean keepUsername = false;
		String texturepack = "";
		String title = "Minecraft (" + NAME + ")";
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
				for (String additionalJar : arg.substring(17).split(",")) {
					if (additionalJar.length() > 0) {
						additionalJars.add(additionalJar);
					}
				}
			} else if (arg.equals("--no-frame")) {
				noFrame = true;
			} else if (arg.startsWith("--parent-dir=")) {
				parentDir = arg.substring(13);
			} else if (arg.startsWith("--applet=")) {
				appletToLoad = arg.substring(9);
			} else if (arg.startsWith("--blend-with=")) {
				for (String blendWithJar : arg.substring(13).split(",")) {
					if (blendWithJar.length() > 0) {
						blendWith.add(blendWithJar);
					}
				}
			} else if (arg.startsWith("--blend-jar-name=")) {
				blendJarName = arg.substring(17);
			} else if (arg.equals("--blend-keep-manifest")) {
				blendKeepManifest = true;
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

		// Check if we were provided with a path, otherwise fall back to the defaults.
		if (jarDir.isEmpty() && jar.isEmpty()) {
			jarDir = new File(new File(parentDir, ".minecraft").toString(), "bin").toString();
		}

		// This is some odd stuff...
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

		// Set the parentDir into the user.home variable.
		// While this seems odd at first, the Minecraft-Applet does
		// read that variable to determine where the .minecraft directory is.
		System.setProperty("user.home", parentDir);

		// This is needed for the Forge ModLoader and maybe others.
		System.setProperty("minecraft.applet.TargetDirectory", parentDir);

		// Extend the parentDir for our own, personal use only.
		parentDir = new File(parentDir, ".minecraft").toString();

		// Shall we read from the lastlogin file?
		if (useLastLogin) {
			LastLogin lastLogin = new LastLogin();
			try {
				lastLogin.readFrom(parentDir);
				username = lastLogin.getUsername();
				password = lastLogin.getPassword();
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, "Reading the lastlogin-file failed!", ex);
			} catch (LastLoginCipherException ex) {
				LOGGER.log(Level.SEVERE, "Reading the lastlogin-file failed!", ex);
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
			System.out.println("applet: " + appletToLoad);
			System.out.println("blendWith: ");
			for (String file : blendWith) {
				System.out.println("	(exists: " + new File(file).exists() + "): " + file);
			}
			System.out.println("blendJarName: " + blendJarName);
			System.out.println("blendKeepManifest: " + blendKeepManifest);
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

		// Will it blend?
		if (!blendWith.isEmpty()) {
			Blender blender = new Blender();
			blender.setKeepManifest(blendKeepManifest);
			blender.add(jar);
			for (String file : blendWith) {
				blender.add(file);
			}

			try {
				blender.blend(blendJarName);
				jar = blendJarName;
			} catch (FileNotFoundException ex) {
				LOGGER.log(Level.SEVERE, "Failed to blend jar!", ex);
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, "Failed to blend jar", ex);
			}
		}

		// Now try if we manage to login...
		if (authenticate) {
			final Authentication authentication = new Authentication(authenticationAddress, launcherVersion, username, password);
			AuthenticationResponse response = AuthenticationResponse.UNKNOWN;
			try {
				response = authentication.authenticate();
			} catch (UnsupportedEncodingException ex) {
				LOGGER.log(Level.SEVERE, "Authentication failed!", ex);
			} catch (MalformedURLException ex) {
				LOGGER.log(Level.SEVERE, "Authentication failed!", ex);
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, "Authentication failed!", ex);
			}
			if (response == AuthenticationResponse.SUCCESS) {
				sessionId = authentication.getSessionId();
				authentication.setKeepAliveUsesRealUsername(!keepUsername);

				if (saveLastLogin) {
					LastLogin lastLogin = new LastLogin(authentication);
					try {
						lastLogin.writeTo(parentDir);
					} catch (IOException ex) {
						LOGGER.log(Level.SEVERE, "Writing the lastlogin file failed!", ex);
					} catch (LastLoginCipherException ex) {
						LOGGER.log(Level.SEVERE, "Writing the lastlogin file failed!", ex);
					}
				}

				// Only launch the keep alive ticker if the login was successfull.
				if (keepAliveTick > 0) {
					Timer timer = new Timer("Authentication Keep Alive", true);
					timer.scheduleAtFixedRate(new TimerTask() {

						@Override
						public void run() {
							try {
								authentication.keepAlive();
							} catch (UnsupportedEncodingException ex) {
								LOGGER.log(Level.SEVERE, "Keep-Alive failed!", ex);
							} catch (MalformedURLException ex) {
								LOGGER.log(Level.SEVERE, "Keep-Alive failed!", ex);
							} catch (IOException ex) {
								LOGGER.log(Level.SEVERE, "Keep-Alive failed!", ex);
							}
						}
					}, keepAliveTick * 1000, keepAliveTick * 1000);
				}
			} else {
				LOGGER.log(Level.SEVERE, "Authentication failed: {0}", response.getMessage());

				// Alert the user
				if (authenticationFailureBehavior == AuthenticationFailureBehavior.ALERT_BREAK
						|| authenticationFailureBehavior == AuthenticationFailureBehavior.ALERT_CONTINUE) {
					JOptionPane.showMessageDialog(new JInternalFrame(), response.getMessage(), "Failed to authenticate...", JOptionPane.ERROR_MESSAGE);
				}
				// STOP!
				if (authenticationFailureBehavior == AuthenticationFailureBehavior.ALERT_BREAK
						|| authenticationFailureBehavior == AuthenticationFailureBehavior.SILENT_BREAK) {
					return;
				}
			}
		}

		// Let's work with the options.txt, shall we?
		OptionsFile optionsFile = new OptionsFile();
		try {
			optionsFile.read(optionsFileFrom);
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Reading of the options-file failed!", ex);
		}

		if (optionsFile.isRead()) {
			// Set the texturepack.
			if (!texturepack.isEmpty()) {
				optionsFile.setOption("skin", texturepack);
			}

			// Set the options.
			if (!options.isEmpty()) {
				optionsFile.setOptions(options);
			}
			try {
				optionsFile.write(parentDir);
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, "Writing of the options-file failed!", ex);
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
			// This might fix issues for Mods which assume that they
			// are loaded via the real launcher...not sure, thought adding
			// it would be a good idea.
			List<URL> urls = new ArrayList<URL>();
			for (String item : additionalJars) {
				try {
					urls.add(new File(item).toURI().toURL());
				} catch (MalformedURLException ex) {
					LOGGER.log(Level.SEVERE, "Failed to convert to URL!", ex);
				}
			}

			try {
				ClassLoaderExtender.extend(urls.toArray(new URL[urls.size() - 1]));
			} catch (NoSuchMethodException ex) {
				LOGGER.log(Level.SEVERE, "Failed to extend ClassLoader!", ex);
			} catch (IllegalAccessException ex) {
				LOGGER.log(Level.SEVERE, "Failed to extend ClassLoader!", ex);
			} catch (IllegalArgumentException ex) {
				LOGGER.log(Level.SEVERE, "Failed to extend ClassLoader!", ex);
			} catch (InvocationTargetException ex) {
				LOGGER.log(Level.SEVERE, "Failed to extend ClassLoader!", ex);
			}
		}

		// Let's tell the Forge ModLoader (and others) that it is supposed
		// to load our applet and not that of the official launcher.
		System.setProperty("minecraft.applet.WrapperClass", "org.bonsaimind.easyminelauncher.ContainerApplet");

		// Create the applet.
		ContainerApplet container = new ContainerApplet(appletToLoad);

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
			LOGGER.log(Level.SEVERE, "Failed to load Minecraft! Exiting.");

			if (noExit) {
				return;
			} else {
				// Exit just to be sure.
				System.exit(0);
			}
		}
	}

	private static void printVersion() {
		System.out.println(NAME + " " + VERSION);
		System.out.println("Copyright 2012 Robert 'Bobby' Zenz. All rights reserved.");
		System.out.println("Licensed under 2-clause-BSD.");
	}

	private static void printHelp() {
		System.out.println("Usage: " + NAME + ".jar [OPTIONS]");
		System.out.println("Launch Minecraft directly.");
		System.out.println("");

		InputStream stream = Main.class.getResourceAsStream("/org/bonsaimind/easyminelauncher/help.text");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			reader.close();
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Failed to read the help-file!", ex);
		}
	}
}
