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
	private static String version = "0.1";

	public static void main(String[] args) {
		String jarDir = "";
		String nativeDir = "";
		String username = "Username";
		String title = "Minecraft (EasyMineLauncher)";
		boolean maximized = false;
		int width = 800;
		int height = 600;

		// Parse arguments
		for (String arg : args) {
			if (arg.startsWith("--jar-dir=")) {
				jarDir = arg.substring(10);
			} else if (arg.startsWith("--native-dir=")) {
				nativeDir = arg.substring(13);
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
		if (jarDir.isEmpty()) {
			printHelp();
			return;
		}
		jarDir = new File(jarDir).getAbsolutePath();
		if (nativeDir.isEmpty()) {
			nativeDir = new File(jarDir, "natives").getAbsolutePath();
		}
		if (height <= 0) {
			height = 600;
		}
		if (width <= 0) {
			width = 800;
		}

		// Create the applet.
		ContainerApplet container = new ContainerApplet();
		container.setUsername(username);

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
		if (container.loadJarsAndApplet(jarDir)) {
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
		System.out.println("  --native-dir=DIRECTORY   Set the directory for the native files.");
		System.out.println("  --username=USERNAME      Set the username to USERNAME.");

		System.out.println("");
		System.out.println("  --title=TITLE            Replace the window title.");
		System.out.println("  --height=HEIGHT          The width of the window.");
		System.out.println("  --width=WIDTH            The height of the window.");
		System.out.println("  --maximized              Maximize the window.");
	}
}
