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
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
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
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	private static String name = "EasyMineLauncher";
	private static String version = "0.6";

	public static void main(String[] args) {
		String jarDir = "";
		String jar = "";
		String lwjglDir = "";
		String mppass = "";
		String nativeDir = "";
		List<String> additionalJars = new ArrayList<String>();
		boolean noFrame = false;
		List<String> options = new ArrayList<String>();
		String parentDir = "";
		String port = null;
		String server = null;
		String sessionId = "0";
		String username = "Username";
		String texturepack = "";
		String title = "Minecraft (" + name + ")";
		boolean maximized = false;
		int width = 800;
		int height = 600;
		boolean alwaysOnTop = false;
		boolean fullscreen = false;

		// Parse arguments
		for (String arg : args) {
			if (arg.startsWith("--jar-dir=")) {
				jarDir = arg.substring(10);
			} else if (arg.startsWith("--jar=")) {
				jar = arg.substring(6);
			} else if (arg.startsWith("--lwjgl-dir=")) {
				lwjglDir = arg.substring(12);
			} else if (arg.startsWith("--mppass=")) {
				mppass = arg.substring(9);
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
			} else if (arg.startsWith("--session-id=")) {
				sessionId = arg.substring(13);
			} else if (arg.startsWith("--set-option=")) {
				options.add(arg.substring(13));
			} else if (arg.startsWith("--texturepack=")) {
				texturepack = arg.substring(14);
			} else if (arg.startsWith("--title=")) {
				title = arg.substring(8);
			} else if (arg.startsWith("--username=")) {
				username = arg.substring(11);
			} else if (arg.equals("--version")) {
				printVersion();
				return;
			} else if (arg.equals("--width=")) {
				width = Integer.parseInt(arg.substring(8));
			} else if (arg.equals("--height=")) {
				height = Integer.parseInt(arg.substring(9));
			} else if (arg.equals("--maximized")) {
				maximized = true;
			} else if (arg.equals("--always-on-top")) {
				alwaysOnTop = true;
			} else if (arg.equals("--fullscreen")) {
				fullscreen = true;
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
			printHelp();
			return;
		}

		System.out.println(System.getProperty("user.home"));

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
		} else {
			parentDir = System.getProperty("user.home");
		}
		parentDir = new File(parentDir, ".minecraft").toString();

		if (!texturepack.isEmpty()) {
			OptionsFile optionsFile = new OptionsFile(parentDir);
			if (optionsFile.exists() && optionsFile.read()) {
				optionsFile.setTexturePack(texturepack);

				// Set the options.
				for (String option : options) {
					int splitIdx = option.indexOf(":");
					if (splitIdx > 0) { // we don't want not-named options either.
						optionsFile.setOption(option.substring(0, splitIdx), option.substring(splitIdx + 1));
					}
				}

				if (!optionsFile.write()) {
					System.out.println("Failed to write options.txt!");
				}
			} else {
				System.out.println("Failed to read options.txt or it does not exist!");
			}
		}

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

		// Create the applet.
		ContainerApplet container = new ContainerApplet();

		// Pass arguments to the applet.
		container.setUsername(username);
		if (server != null) {
			container.setServer(server, port != null ? port : "25565");
		}
		container.setMpPass(mppass);
		container.setSessionId(sessionId);
		// Create and set up the frame.
		ContainerFrame frame = new ContainerFrame(title);
		if (fullscreen) {
			Dimension dimensions = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setAlwaysOnTop(true);
			frame.setUndecorated(true);
			frame.setSize(dimensions.width, dimensions.height);
		} else {
			frame.setAlwaysOnTop(alwaysOnTop);
			frame.setUndecorated(noFrame);
			frame.setSize(width, height);
			if (maximized) {
				frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			}
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

			// Exit just to be sure.
			System.exit(0);
		}
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

	private static void printVersion() {
		System.out.println(name + " " + version);
		System.out.println("Copyright 2012 Robert 'Bobby' Zenz. All rights reserved.");
		System.out.println("Licensed under 2-clause-BSD.");
	}

	private static void printHelp() {
		System.out.println("Usage: " + name + ".jar [OPTION]");
		System.out.println("Launch Minecraft directly.");
		System.out.println("");

		System.out.println("  --help                   Prints this help.");
		System.out.println("  --version                Prints the version.");
		System.out.println("");

		System.out.println("  --jar-dir=DIRECTORY      Set the directory for the jar files.");
		System.out.println("  --jar=MINECRAFT.JAR      Set the path to the minecraft.jar.");
		System.out.println("                           Either specify jar-dir or this.");
		System.out.println("  --lwjgl-dir=DIRECTORY    Set the directory for the jar files");
		System.out.println("                           of lwjgl (lwjgl.jar, lwjgl_util.jar,");
		System.out.println("                           and jinput.jar)");
		System.out.println("  --mppass=MPPASS          Set the mppass variable.");
		System.out.println("  --native-dir=DIRECTORY   Set the directory for the native files");
		System.out.println("                           of lwjgl.");
		System.out.println("  --additoinal-jar=JAR     Load the specified jars.");
		System.out.println("                           This might be needed by some mods.");
		System.out.println("                           Specify multiple times or list separated");
		System.out.println("                           with ','.");
		System.out.println("  --parent-dir=DIRECTORY   Set the parent directory. This effectively");
		System.out.println("                           changes the location of the .minecraft folder.");
		System.out.println("  --port=PORT              Set the port of the server, if not set");
		System.out.println("                           it will revert to 25565.");
		System.out.println("  --texturepack=FILE       Set the texturepack to use, this takes");
		System.out.println("                           only the filename (including extension).");
		System.out.println("                           Use 'Default' for default.");
		System.out.println("  --server=SERVER          Set the address of the server which");
		System.out.println("                           directly to connect to.");
		System.out.println("  --session-id=SESSIONID   Set the session id.");
		System.out.println("  --set-option=NAME:VALUE  Set this option in the options.txt file.");
		System.out.println("  --username=USERNAME      Set the username to user.");

		System.out.println("");
		System.out.println("  --title=TITLE            Replace the window title.");
		System.out.println("  --height=HEIGHT          The width of the window.");
		System.out.println("  --width=WIDTH            The height of the window.");
		System.out.println("  --maximized              Maximize the window.");
		System.out.println("  --no-frame               Remove the border of the window.");
		System.out.println("  --always-on-top          Make the window stay above all others.");
		System.out.println("  --fullscreen             Makes the window the same size as the");
		System.out.println("                           specified monitor or the whole desktop.");
		System.out.println("                           This is basically shorthand for");
		System.out.println("                             --width=SCREENWIDTH");
		System.out.println("                             --height=SCREENHEIGHT");
		System.out.println("                             --no-frame");
		System.out.println("                             --always-on-top");
		System.out.println("                           This might yield odd results in multi-");
		System.out.println("                           monitor environments.");
	}
}
