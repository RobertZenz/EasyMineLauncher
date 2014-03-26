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
import org.bonsaimind.minecraftmiddleknife.Blender;
import org.bonsaimind.minecraftmiddleknife.ClassLoaderExtender;
import org.bonsaimind.minecraftmiddleknife.ClassLoaderExtensionException;
import org.bonsaimind.minecraftmiddleknife.LastLogin;
import org.bonsaimind.minecraftmiddleknife.LastLoginCipherException;
import org.bonsaimind.minecraftmiddleknife.OptionsFile;
import org.bonsaimind.minecraftmiddleknife.pre16.AppletLoadException;
import org.bonsaimind.minecraftmiddleknife.pre16.Authentication;
import org.bonsaimind.minecraftmiddleknife.pre16.AuthenticationResponse;
import org.bonsaimind.minecraftmiddleknife.pre16.ContainerApplet;
import org.bonsaimind.minecraftmiddleknife.pre16.ContainerFrame;

public class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	private static final String NAME = "EasyMineLauncher";
	private static final String VERSION = "0.16.1";

	public static void main(String[] args) {
		Arguments arguments = new Arguments(args);

		if (arguments.isPrintHelp()) {
			printHelp();
			return;
		}

		if (arguments.isPrintVersion()) {
			printVersion();
			return;
		}

		// Set the parentDir into the user.home variable.
		// While this seems odd at first, the Minecraft-Applet does
		// read that variable to determine where the .minecraft directory is.
		System.setProperty("user.home", arguments.getParentDir());

		// This is needed for the Forge ModLoader and maybe others.
		System.setProperty("minecraft.applet.TargetDirectory", arguments.getParentDir());

		// Extend the parentDir for our own, personal use only.
		arguments.setParentDir(new File(arguments.getParentDir(), ".minecraft").toString());

		// Shall we read from the lastlogin file?
		if (arguments.isUseLastLogin()) {
			arguments = readLastLogin(arguments);
		}

		if (arguments.isPrintDump()) {
			System.out.println(arguments.toString());
			return;
		}

		// Will it blend?
		if (!arguments.getBlendWith().isEmpty()) {
			arguments = blendJar(arguments);
		}

		// Now try if we manage to login...
		if (arguments.isAuthenticate()) {
			final Authentication authentication = new Authentication(arguments.getAuthenticationAddress(), arguments.getLauncherVersion(), arguments.getUsername(), arguments.getPassword());
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
				arguments.setSessionId(authentication.getSessionId());
				authentication.setKeepAliveUsesRealUsername(!arguments.isKeepUsername());

				if (arguments.isSaveLastLogin()) {
					LastLogin lastLogin = new LastLogin(authentication);
					try {
						lastLogin.writeTo(arguments.getParentDir());
					} catch (IOException ex) {
						LOGGER.log(Level.SEVERE, "Writing the lastlogin file failed!", ex);
					} catch (LastLoginCipherException ex) {
						LOGGER.log(Level.SEVERE, "Writing the lastlogin file failed!", ex);
					}
				}

				// Only launch the keep alive ticker if the login was successfull.
				if (arguments.getKeepAliveTick() > 0) {
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
					}, arguments.getKeepAliveTick() * 1000, arguments.getKeepAliveTick() * 1000);
				}
			} else {
				LOGGER.log(Level.SEVERE, "Authentication failed: {0}", response.getMessage());

				// Alert the user
				if (arguments.getAuthenticationFailureBehavior() == AuthenticationFailureBehavior.ALERT_BREAK
						|| arguments.getAuthenticationFailureBehavior() == AuthenticationFailureBehavior.ALERT_CONTINUE) {
					JOptionPane.showMessageDialog(new JInternalFrame(), response.getMessage(), "Failed to authenticate...", JOptionPane.ERROR_MESSAGE);
				}
				// STOP!
				if (arguments.getAuthenticationFailureBehavior() == AuthenticationFailureBehavior.ALERT_BREAK
						|| arguments.getAuthenticationFailureBehavior() == AuthenticationFailureBehavior.SILENT_BREAK) {
					return;
				}
			}
		}

		if (!arguments.getTexturepack().isEmpty() || !arguments.getOptions().isEmpty() || !arguments.getOptionsFileFrom().isEmpty()) {
			if (arguments.getOptionsFileFrom().isEmpty()) {
				arguments.setOptionsFileFrom(new File(arguments.getParentDir(), "options.txt").getAbsolutePath());
			}

			setOptions(arguments);
		}

		// Load the launcher
		if (!arguments.getAdditionalJars().isEmpty()) {
			loadAdditionalJars(arguments);
		}

		// Let's tell the Forge ModLoader (and others) that it is supposed
		// to load our applet and not that of the official launcher.
		System.setProperty("minecraft.applet.WrapperClass", "org.bonsaimind.easyminelauncher.ContainerApplet");

		ContainerFrame frame = createFrame(arguments);
		ContainerApplet applet = createApplet(arguments);

		frame.setContainerApplet(applet);
		frame.setVisible(true);

		// Load
		applet.loadNatives(arguments.getNativeDir());
		try {
			applet.loadJarsAndApplet(arguments.getJar(), arguments.getLwjglDir());
			applet.init();
			applet.start();
		} catch (AppletLoadException ex) {
			LOGGER.log(Level.SEVERE, "Failed to load Minecraft!", ex);

			if (arguments.isNoExit()) {
				return;
			} else {
				// Exit just to be sure.
				System.exit(0);
			}
		}
	}

	private static ContainerApplet createApplet(Arguments arguments) {
		ContainerApplet applet = new ContainerApplet(arguments.getAppletToLoad());
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

	private static ContainerFrame createFrame(Arguments arguments) {
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
			frame.setLocation(
					arguments.getX() == -1 ? frame.getX() : arguments.getX(),
					arguments.getY() == -1 ? frame.getY() : arguments.getY());

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

	private static Arguments blendJar(Arguments arguments) {
		Blender blender = new Blender();
		blender.setKeepManifest(arguments.isBlendKeepManifest());
		blender.add(arguments.getJar());
		for (String file : arguments.getBlendWith()) {
			blender.add(file);
		}

		try {
			blender.blend(arguments.getBlendJarName());
			arguments.setJar(arguments.getBlendJarName());
		} catch (FileNotFoundException ex) {
			LOGGER.log(Level.SEVERE, "Failed to blend jar!", ex);
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Failed to blend jar", ex);
		}

		return arguments;
	}

	private static void loadAdditionalJars(Arguments arguments) {
		// This might fix issues for Mods which assume that they
		// are loaded via the real launcher...not sure, thought adding
		// it would be a good idea.
		List<URL> urls = new ArrayList<URL>();
		for (String item : arguments.getAdditionalJars()) {
			try {
				urls.add(new File(item).toURI().toURL());
			} catch (MalformedURLException ex) {
				LOGGER.log(Level.SEVERE, "Failed to convert to URL!", ex);
			}
		}

		try {
			ClassLoaderExtender.extend(urls.toArray(new URL[urls.size() - 1]));
		} catch (ClassLoaderExtensionException ex) {
			LOGGER.log(Level.SEVERE, "Failed to extend ClassLoader!", ex);
		}
	}

	private static Arguments readLastLogin(Arguments arguments) {
		LastLogin lastLogin = new LastLogin();
		try {
			lastLogin.readFrom(arguments.getParentDir());
			arguments.setUsername(lastLogin.getUsername());
			arguments.setPassword(lastLogin.getPassword());
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Reading the lastlogin-file failed!", ex);
		} catch (LastLoginCipherException ex) {
			LOGGER.log(Level.SEVERE, "Reading the lastlogin-file failed!", ex);
		}

		return arguments;
	}

	private static void setOptions(Arguments arguments) {
		OptionsFile optionsFile = new OptionsFile();
		try {
			optionsFile.read(arguments.getOptionsFileFrom());
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Reading of the options-file failed!", ex);
		}

		if (optionsFile.isRead()) {
			// Set the texturepack.
			if (!arguments.getTexturepack().isEmpty()) {
				optionsFile.setOption("skin", arguments.getTexturepack());
			}

			// Set the options.
			if (!arguments.getOptions().isEmpty()) {
				optionsFile.setOptions(arguments.getOptions());
			}
			try {
				optionsFile.write(arguments.getParentDir());
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, "Writing of the options-file failed!", ex);
			}
		}
	}
}
