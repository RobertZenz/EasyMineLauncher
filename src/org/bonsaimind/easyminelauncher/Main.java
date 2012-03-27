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

import java.awt.Frame;
import java.io.File;

public class Main {

	private static String name = "EasyMineLauncher";
	private static String version = "0.3";

	public static void main(String[] args) {
		String jarDir = "";
		String jar = "";
		String lwjglDir = "";
		String nativeDir = "";
		String parentDir = "";
		String port = null;
		String server = null;
		String username = "Username";
		String title = "Minecraft (EasyMineLauncher)";
		boolean maximized = false;
		int width = 800;
		int height = 600;

		// Parse arguments
		for (String arg : args) {
			if (arg.startsWith("--jar-dir=")) {
				jarDir = arg.substring(10);
			} else if (arg.startsWith("--jar=")) {
				jar = arg.substring(6);
			} else if (arg.startsWith("--lwjgl-dir=")) {
				lwjglDir = arg.substring(12);
			} else if (arg.startsWith("--native-dir=")) {
				nativeDir = arg.substring(13);
			} else if (arg.startsWith("--parent-dir=")) {
				parentDir = arg.substring(13);
			} else if (arg.startsWith("--port=")) {
				port = arg.substring(7);
			} else if (arg.startsWith("--server=")) {
				server = arg.substring(9);
			} else if (arg.startsWith("--username=")) {
				username = arg.substring(11);
			} else if (arg.startsWith("--title=")) {
				title = arg.substring(8);
			} else if (arg.equals("--version")) {
				printVersion();
				return;
			} else if (arg.equals("--width=")) {
				width = Integer.parseInt(arg.substring(8));
			} else if (arg.equals("--height=")) {
				height = Integer.parseInt(arg.substring(9));
			} else if (arg.equals("--maximized")) {
				maximized = true;
			} else if (arg.equals("--help")) {
				printHelp();
				return;
			} else {
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
		if(!parentDir.isEmpty()) {
			System.setProperty("user.home", parentDir);
		}
		if (height <= 0) {
			height = 600;
		}
		if (width <= 0) {
			width = 800;
		}

		// Create the applet.
		ContainerApplet container = new ContainerApplet();

		// Pass arguments to the applet.
		container.setUsername(username);
		if (server != null) {
			container.setServer(server, port != null ? port : "25565");
		}

		// Create and setup the frame.
		ContainerFrame frame = new ContainerFrame(title);
		frame.setSize(width, height);
		if (maximized) {
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
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

	private static void printVersion() {
		System.out.println(name + " " + version);
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
		System.out.println("  --native-dir=DIRECTORY   Set the directory for the native files");
		System.out.println("                           of lwjgl.");
		System.out.println("  --parent-dir=DIRECTORY   Set the parent directory. This effectively");
		System.out.println("                           changes the location of the .minecraft folder.");
		System.out.println("  --port=PORT              Set the port of the server, if not set");
		System.out.println("                           it will revert to 25565.");
		System.out.println("  --server=SERVER          Set the address of the server which");
		System.out.println("                           directly to connect to.");
		System.out.println("  --username=USERNAME      Set the username to user.");

		System.out.println("");
		System.out.println("  --title=TITLE            Replace the window title.");
		System.out.println("  --height=HEIGHT          The width of the window.");
		System.out.println("  --width=WIDTH            The height of the window.");
		System.out.println("  --maximized              Maximize the window.");
	}
}
