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

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the main container for the MinecraftApplet.
 * It's usage is simple, create it, set the username,
 * load the natives, load the jars, init, start.
 */
public class ContainerApplet extends Applet
		implements AppletStub {

	private Map<String, String> parameters = new HashMap<String, String>();
	private Applet minecraftApplet;

	/**
	 * Create an instance.
	 * @throws HeadlessException
	 */
	public ContainerApplet() throws HeadlessException {
		super();

		setLayout(new BorderLayout());

		parameters.put("fullscreen", "false");
		parameters.put("stand-alone", "true");
		parameters.put("username", "Username");
		parameters.put("mppass", "");
		parameters.put("server", null);
		parameters.put("port", null);
		parameters.put("sessionid", "0");
	}

	public void appletResize(int width, int height) {
	}

	@Override
	public void destroy() {
		if (minecraftApplet != null) {
			minecraftApplet.destroy();
		}
		super.destroy();
	}

	@Override
	public URL getDocumentBase() {
		try {
			return new URL("http://localhost:0");
		} catch (MalformedURLException ex) {
			System.err.println(ex);
		}

		return null;
	}

	@Override
	public String getParameter(String name) {
		System.out.print("Parameter requested: " + name + "...");

		if (parameters.containsKey(name)) {
			System.out.println(parameters.get(name));
			return parameters.get(name);
		} else {
			System.out.println("UNHANDLED!");
			return "";
		}
	}

	@Override
	public boolean isActive() {
		// I'm not sure what this is, but it makes it work.
		return true;
	}

	/**
	 * Load the 4 jars and create an instance of the MinecraftApplet.
	 * Better call loadNatives(String) first.
	 * @param jarDir The directory of the jars.
	 * @return
	 */
	public boolean loadJarsAndApplet(String jarDir) {
		System.out.println("Loading Minecraft from: " + jarDir);

		try {
			// Our 4 jars which we need.
			URL[] urls = new URL[]{
				new URL("file://" + new File(jarDir, "minecraft.jar").getAbsolutePath()),
				new URL("file://" + new File(jarDir, "lwjgl.jar").getAbsolutePath()),
				new URL("file://" + new File(jarDir, "lwjgl_util.jar").getAbsolutePath()),
				new URL("file://" + new File(jarDir, "jinput.jar").getAbsolutePath())
			};

			// Load the jars.
			URLClassLoader loader = new URLClassLoader(urls);

			// Create the MinecraftApplet
			minecraftApplet = (Applet) loader.loadClass("net.minecraft.client.MinecraftApplet").newInstance();

			// Set the size, otherwise LWJGL will fail to initialize the Display.
			minecraftApplet.setSize(getWidth(), getHeight());

			// We're it's...stub...
			minecraftApplet.setStub(this);

			// Add it...what else?
			add(minecraftApplet, "Center");

			return true;
		} catch (ClassNotFoundException ex) {
			System.err.println(ex);
		} catch (InstantiationException ex) {
			System.err.println(ex);
		} catch (IllegalAccessException ex) {
			System.err.println(ex);
		} catch (MalformedURLException ex) {
			System.err.println(ex);
		}

		return false;
	}

	/**
	 * Load the native libraries.
	 * @param nativeDir The directory which contains the native LWJGL libraries.
	 */
	public void loadNatives(String nativeDir) {
		System.out.println("Loading natives from: " + nativeDir);

		System.setProperty("org.lwjgl.librarypath", nativeDir);
		System.setProperty("net.java.games.input.librarypath", nativeDir);
	}

	@Override
	public void init() {
		minecraftApplet.init();
	}

	/**
	 * Set the username.
	 *
	 * @param username The username
	 */
	public void setUsername(String username) {
		parameters.put("username", username);
	}

	@Override
	public void start() {
		minecraftApplet.start();
	}

	@Override
	public void stop() {
		if (minecraftApplet != null) {
			minecraftApplet.stop();
		}

		super.stop();
	}
}
