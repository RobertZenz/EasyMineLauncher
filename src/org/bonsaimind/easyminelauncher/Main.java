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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	private static String version = "0.11";

	public static void main(String[] args) {
		String jarDir = "";
		String jar = "";
		String lwjglDir = "";
		String mppass = "";
		String nativeDir = "";
		List<String> additionalJars = new ArrayList<String>();
		boolean noFrame = false;
		String optionsFileFrom = "";
		List<String> options = new ArrayList<String>();
		boolean demo = false;
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
		int x = -1;
		int y = -1;
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


		// Let's work with the options.txt, shall we?
		OptionsFile optionsFile = new OptionsFile(parentDir);
		if (!optionsFileFrom.isEmpty()) {
			optionsFile.setPath(optionsFileFrom);
		}

		if (!optionsFile.exists() || !optionsFile.read()) {
			// Reset the path in case we used an external options.txt.
			optionsFile.setPath(parentDir);
		} else {
			System.out.println("Failed to read options.txt from \"" + optionsFile + "\" or it does not exist!");
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
}
