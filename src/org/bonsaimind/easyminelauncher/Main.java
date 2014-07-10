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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
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
import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.AuthenticationResponse;
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
	
	private static Parameters blendJar(Parameters arguments) {
		Blender blender = new Blender();
		blender.setKeepManifest(arguments.isBlendKeepManifest());
		blender.add(arguments.getJar());
		for (String file : arguments.getBlendWith()) {
			blender.add(file);
		}
		
		try {
			blender.blend(arguments.getBlendJarName());
			arguments.setJar(arguments.getBlendJarName());
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Failed to blend jar!", e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to blend jar", e);
		}
		
		return arguments;
	}
	
	private static ContainerApplet createApplet(Parameters arguments) {
		ContainerApplet applet = new ContainerApplet(arguments.getAppletToLoad(), createClassLoader(arguments));
		applet.setParameter(ContainerApplet.PARAMETER_DEMO, Boolean.toString(arguments.isDemo()));
		applet.setParameter(ContainerApplet.PARAMETER_USERNAME, arguments.getUsername());
		applet.setParameter(ContainerApplet.PARAMETER_LOADMAP_USER, arguments.getUsername());
		if (arguments.getServer() != null) {
			applet.setParameter(ContainerApplet.PARAMETER_SERVER, arguments.getServer());
			applet.setParameter(ContainerApplet.PARAMETER_PORT, arguments.getPort());
		}
		applet.setParameter(ContainerApplet.PARAMETER_MPPASS, arguments.getPassword());
		applet.setParameter(ContainerApplet.PARAMETER_SESSION_ID, arguments.getSessionId());
		return applet;
	}
	
	private static ClassLoader createClassLoader(Parameters arguments) {
		ClassLoaderCreator creator = new ClassLoaderCreator();
		
		try {
			creator.addJar(arguments.getJar());
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "Failed to convert to URL!", e);
		}
		
		for (String jar : arguments.getAdditionalJars()) {
			try {
				creator.addJar(jar);
			} catch (MalformedURLException e) {
				LOGGER.log(Level.SEVERE, "Failed to convert to URL!", e);
			}
		}
		
		return creator.createClassLoader();
	}
	
	private static ContainerFrame createFrame(Parameters arguments) {
		ContainerFrame frame = new ContainerFrame(arguments.getTitle());
		frame.setExitOnClose(!arguments.isNoExit());
		
		if (arguments.isFullscreen()) {
			Dimension dimensions = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setAlwaysOnTop(true);
			frame.setUndecorated(true);
			frame.setSize(dimensions.width, dimensions.height);
			frame.setLocation(0, 0);
		} else {
			frame.setAlwaysOnTop(arguments.isAlwaysOnTop());
			frame.setUndecorated(arguments.isNoFrame());
			frame.setSize(arguments.getWidth(), arguments.getHeight());
			
			// It is more likely that no location is set...I think.
			frame.setLocation(arguments.getX() == -1 ? frame.getX() : arguments.getX(), arguments.getY() == -1 ? frame.getY() : arguments.getY());
			
			if (arguments.isMaximized()) {
				frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			}
		}
		
		if (arguments.getOpacity() < 1) {
			frame.setUndecorated(true);
			frame.setOpacity(arguments.getOpacity());
		}
		
		return frame;
	}
	
	private static Parameters doAuthentication(Parameters parameters) {
		Credentials credentials = new Credentials(parameters.getUsername(), parameters.getPassword());
		final URL authenticationUrl = new URL(parameters.getAuthenticationAddress());
		final AuthenticatedSession session = Authenticator.authenticate(authenticationUrl, parameters.getLauncherVersion(), credentials);
		
		// TODO overwrite username in case that parameters.isKeepusername is
		// set.
		
		parameters.setSessionId(session.getSessionId());
		
		if (parameters.getKeepAliveTick() > 0) {
			Timer timer = new Timer("Authentication Keep Alive", true);
			timer.scheduleAtFixedRate(new TimerTask() {
				
				@Override
				public void run() {
					Authenticator.keepAlive(authenticationUrl, session);
				}
			}, parameters.getKeepAliveTick() * 1000, parameters.getKeepAliveTick() * 1000);
		}
		
		authentication.setKeepAliveUsesRealUsername(!parameters.isKeepUsername());
		
		if (parameters.isSaveLastLogin()) {
			LastLogin lastLogin = new LastLogin();
			lastLogin.writeCredentials(parameters.getParentDir(), credentials);
		}
		
		final Authentication authentication = new Authentication(parameters.getAuthenticationAddress(), parameters.getLauncherVersion(),
				parameters.getUsername(), parameters.getPassword());
		AuthenticationResponse response = AuthenticationResponse.UNKNOWN;
		try {
			response = authentication.authenticate();
		} catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, "Authentication failed!", e);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "Authentication failed!", e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Authentication failed!", e);
		}
		if (response == AuthenticationResponse.SUCCESS) {
			parameters.setSessionId(authentication.getSessionId());
			authentication.setKeepAliveUsesRealUsername(!parameters.isKeepUsername());
			
			// Only launch the keep alive ticker if the login was
			// successfull.
			if (parameters.getKeepAliveTick() > 0) {
				Timer timer = new Timer("Authentication Keep Alive", true);
				timer.scheduleAtFixedRate(new TimerTask() {
					
					@Override
					public void run() {
						try {
							authentication.keepAlive();
						} catch (UnsupportedEncodingException e) {
							LOGGER.log(Level.SEVERE, "Keep-Alive failed!", e);
						} catch (MalformedURLException e) {
							LOGGER.log(Level.SEVERE, "Keep-Alive failed!", e);
						} catch (IOException e) {
							LOGGER.log(Level.SEVERE, "Keep-Alive failed!", e);
						}
					}
				}, parameters.getKeepAliveTick() * 1000, parameters.getKeepAliveTick() * 1000);
			}
		} else {
			LOGGER.log(Level.SEVERE, "Authentication failed: {0}", response.getMessage());
			
			// Alert the user
			if (parameters.getAuthenticationFailureBehavior().isAlert()) {
				JOptionPane.showMessageDialog(new JInternalFrame(), response.getMessage(), "Failed to authenticate...", JOptionPane.ERROR_MESSAGE);
			}
			// STOP!
			if (parameters.getAuthenticationFailureBehavior().isBreak()) {
				return;
			}
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
	
	private static Parameters readLastLogin(Parameters arguments) {
		LastLogin lastLogin = new LastLogin();
		try {
			Credentials credentials = lastLogin.readCredentials(arguments.getParentDir());
			arguments.setUsername(credentials.getUsername());
			arguments.setPassword(credentials.getPassword());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Reading the lastlogin-file failed!", e);
		} catch (LastLoginCipherException e) {
			LOGGER.log(Level.SEVERE, "Reading the lastlogin-file failed!", e);
		}
		
		return arguments;
	}
	
	private static void setOptions(Parameters arguments) {
		OptionsFile optionsFile = new OptionsFile();
		try {
			optionsFile.read(arguments.getOptionsFileFrom());
			
			// Set the texturepack.
			if (!arguments.getTexturepack().isEmpty()) {
				optionsFile.setOption("skin", arguments.getTexturepack());
			}
			
			// Set the options.
			if (!arguments.getOptions().isEmpty()) {
				optionsFile.setOptions(arguments.getOptions());
			}
			
			optionsFile.write(arguments.getParentDir());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Reading of the options-file failed!", e);
		}
	}
}
