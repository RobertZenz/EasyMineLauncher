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
 * THIS SOFTWARE IS PROVIDED BY Robert 'Bobby' Zenz ''AS IS'' AND ANY ePRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Robert 'Bobby' Zenz OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, eEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either epressed
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import org.bonsaimind.minecraftmiddleknife.Blender;
import org.bonsaimind.minecraftmiddleknife.ClassLoaderCreator;
import org.bonsaimind.minecraftmiddleknife.Credentials;
import org.bonsaimind.minecraftmiddleknife.LastLogin;
import org.bonsaimind.minecraftmiddleknife.LastLoginCipherException;
import org.bonsaimind.minecraftmiddleknife.NativeLoader;
import org.bonsaimind.minecraftmiddleknife.OptionsFile;
import org.bonsaimind.minecraftmiddleknife.pre16.AppletLoadException;
import org.bonsaimind.minecraftmiddleknife.pre16.AuthenticatedSession;
import org.bonsaimind.minecraftmiddleknife.pre16.Authenticator;
import org.bonsaimind.minecraftmiddleknife.pre16.ContainerApplet;
import org.bonsaimind.minecraftmiddleknife.pre16.ContainerFrame;

public class Main {
	
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	private static final String NAME = "EasyMineLauncher";
	private static final String VERSION = "0.16.1";
	
	public static void main(String[] args) {
		Parameters parameters = new Parameters(args);
		
		if (parameters.isPrintHelp()) {
			printHelp();
			return;
		}
		
		if (parameters.isPrintVersion()) {
			printVersion();
			return;
		}
		
		// Set the parentDir into the user.home variable.
		// While this seems odd at first, the Minecraft-Applet does
		// read that variable to determine where the .minecraft directory is.
		System.setProperty("user.home", parameters.getParentDir());
		
		// This is needed for the Forge ModLoader and maybe others.
		System.setProperty("minecraft.applet.TargetDirectory", parameters.getParentDir());
		
		// extend the parentDir for our own, personal use only.
		parameters.setParentDir(new File(parameters.getParentDir(), ".minecraft").toString());
		
		// Shall we read from the lastlogin file?
		if (parameters.isUseLastLogin()) {
			parameters = readLastLogin(parameters);
		}
		
		if (parameters.isPrintDump()) {
			System.out.println(parameters.toString());
			return;
		}
		
		// Will it blend?
		if (!parameters.getBlendWith().isEmpty()) {
			parameters = blendJar(parameters);
		}
		
		// Now try if we manage to login...
		if (parameters.isAuthenticate()) {
			parameters = doAuthentication(parameters);
			if (parameters.isExitRequested()) {
				return;
			}
		}
		
		if (!parameters.getTexturepack().isEmpty() || !parameters.getOptions().isEmpty() || !parameters.getOptionsFileFrom().isEmpty()) {
			if (parameters.getOptionsFileFrom().isEmpty()) {
				parameters.setOptionsFileFrom(new File(parameters.getParentDir(), "options.txt").getAbsolutePath());
			}
			
			setOptions(parameters);
		}
		
		// Let's tell the Forge ModLoader (and others) that it is supposed
		// to load our applet and not that of the official launcher.
		System.setProperty("minecraft.applet.WrapperClass", ContainerApplet.class.getCanonicalName());
		
		NativeLoader.loadNativeLibraries(parameters.getNativeDir());
		
		ContainerFrame frame = createFrame(parameters);
		ContainerApplet applet = createApplet(parameters);
		
		frame.setContainerApplet(applet);
		frame.setExitOnClose(true);
		frame.setVisible(true);
		
		// Load
		try {
			applet.loadMinecraftApplet();
			applet.init();
			applet.start();
		} catch (AppletLoadException e) {
			LOGGER.log(Level.SEVERE, "Failed to load Minecraft!", e);
			
			if (parameters.isNoExit()) {
				return;
			}
			
			// exit just to be sure.
			System.exit(0);
		}
	}
	
	private static Parameters blendJar(Parameters parameters) {
		Blender blender = new Blender();
		blender.setKeepManifest(parameters.isBlendKeepManifest());
		blender.add(parameters.getJar());
		for (String file : parameters.getBlendWith()) {
			blender.add(file);
		}
		
		try {
			blender.blend(parameters.getBlendJarName());
			parameters.setJar(parameters.getBlendJarName());
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Failed to blend jar!", e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to blend jar", e);
		}
		
		return parameters;
	}
	
	private static ContainerApplet createApplet(Parameters parameters) {
		ContainerApplet applet = new ContainerApplet(parameters.getAppletToLoad(), createClassLoader(parameters));
		applet.setParameter(ContainerApplet.PARAMETER_DEMO, Boolean.toString(parameters.isDemo()));
		applet.setParameter(ContainerApplet.PARAMETER_USERNAME, parameters.getUsername());
		applet.setParameter(ContainerApplet.PARAMETER_LOADMAP_USER, parameters.getUsername());
		if (parameters.getServer() != null) {
			applet.setParameter(ContainerApplet.PARAMETER_SERVER, parameters.getServer());
			applet.setParameter(ContainerApplet.PARAMETER_PORT, parameters.getPort());
		}
		applet.setParameter(ContainerApplet.PARAMETER_MPPASS, parameters.getPassword());
		applet.setParameter(ContainerApplet.PARAMETER_SESSION_ID, parameters.getSessionId());
		return applet;
	}
	
	private static ClassLoader createClassLoader(Parameters parameters) {
		ClassLoaderCreator creator = new ClassLoaderCreator();
		
		try {
			creator.add(parameters.getJar());
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "Failed to convert to URL!", e);
		}
		
		for (String jar : parameters.getAdditionalJars()) {
			try {
				creator.add(jar);
			} catch (MalformedURLException e) {
				LOGGER.log(Level.SEVERE, "Failed to convert to URL!", e);
			}
		}
		
		return creator.createClassLoader();
	}
	
	private static ContainerFrame createFrame(Parameters parameters) {
		ContainerFrame frame = new ContainerFrame(parameters.getTitle());
		frame.setExitOnClose(!parameters.isNoExit());
		
		if (parameters.isFullscreen()) {
			Dimension dimensions = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setAlwaysOnTop(true);
			frame.setUndecorated(true);
			frame.setSize(dimensions.width, dimensions.height);
			frame.setLocation(0, 0);
		} else {
			frame.setAlwaysOnTop(parameters.isAlwaysOnTop());
			frame.setUndecorated(parameters.isNoFrame());
			frame.setSize(parameters.getWidth(), parameters.getHeight());
			
			// It is more likely that no location is set...I think.
			frame.setLocation(parameters.getX() == -1 ? frame.getX() : parameters.getX(), parameters.getY() == -1 ? frame.getY() : parameters.getY());
			
			if (parameters.isMaximized()) {
				frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			}
		}
		
		if (parameters.getOpacity() < 1) {
			frame.setUndecorated(true);
			frame.setOpacity(parameters.getOpacity());
		}
		
		return frame;
	}
	
	private static Parameters doAuthentication(Parameters parameters) {
		Credentials credentials = new Credentials(parameters.getUsername(), parameters.getPassword());
		AuthenticatedSession session = null;
		
		try {
			URL authenticationUrl = new URL(parameters.getAuthenticationAddress());
			session = Authenticator.authenticate(authenticationUrl, parameters.getLauncherVersion(), credentials);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to authenticate.", e);
			
			// Alert the user
			if (parameters.getAuthenticationFailureBehavior().isAlert()) {
				JOptionPane.showMessageDialog(new JInternalFrame(), e.getMessage(), "Failed to authenticate...", JOptionPane.ERROR_MESSAGE);
			}
			
			// STOP!
			if (parameters.getAuthenticationFailureBehavior().isBreak()) {
				parameters.setExitRequested(true);
			}
			
			return parameters;
		}
		
		parameters.setSessionId(session.getSessionId());
		
		if (parameters.isKeepUsername()) {
			session.setUsernameOverwrite(parameters.getUsername());
		}
		
		if (parameters.getKeepAliveTick() > 0) {
			long tick = parameters.getKeepAliveTick() * 1000;
			Authenticator.scheduleKeepAlive(session, tick, tick);
		}
		
		return parameters;
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
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to read the help-file!", e);
		}
	}
	
	private static void printVersion() {
		System.out.println(NAME + " " + VERSION);
		System.out.println("Copyright 2012 Robert 'Bobby' Zenz. All rights reserved.");
		System.out.println("Licensed under 2-clause-BSD.");
	}
	
	private static Parameters readLastLogin(Parameters parameters) {
		LastLogin lastLogin = new LastLogin();
		try {
			Credentials credentials = lastLogin.readCredentials(parameters.getParentDir());
			parameters.setUsername(credentials.getUsername());
			parameters.setPassword(credentials.getPassword());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Reading the lastlogin-file failed!", e);
		} catch (LastLoginCipherException e) {
			LOGGER.log(Level.SEVERE, "Reading the lastlogin-file failed!", e);
		}
		
		return parameters;
	}
	
	private static void setOptions(Parameters parameters) {
		OptionsFile optionsFile = new OptionsFile();
		try {
			optionsFile.read(parameters.getOptionsFileFrom());
			
			// Set the texturepack.
			if (!parameters.getTexturepack().isEmpty()) {
				optionsFile.setOption("skin", parameters.getTexturepack());
			}
			
			// Set the options.
			if (!parameters.getOptions().isEmpty()) {
				optionsFile.setOptions(parameters.getOptions());
			}
			
			optionsFile.write(parameters.getParentDir());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Reading of the options-file failed!", e);
		}
	}
}
