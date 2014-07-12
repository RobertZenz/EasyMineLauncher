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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bonsaimind.minecraftmiddleknife.pre16.Authenticator;

/**
 * A simple helper class to deal with the commandline arguments.
 */
public class Parameters {
	
	private List<String> additionalJars = new ArrayList<String>();
	private boolean alwaysOnTop = false;
	private String appletToLoad = "net.minecraft.client.MinecraftApplet";
	private boolean authenticate = false;
	private String authenticationAddress = Authenticator.MOJANG_SERVER;
	private AuthenticationFailureBehavior authenticationFailureBehavior = AuthenticationFailureBehavior.ALERT_BREAK;
	private String blendJarName = "minecraft_blended.jar";
	private boolean blendKeepManifest = false;
	private List<String> blendWith = new ArrayList<String>();
	private boolean demo = false;
	private boolean exitRequested = false;
	private boolean fullscreen = false;
	private int height = 600;
	private String jar = "";
	private String jarDir = "";
	private int keepAliveTick = 300;
	private boolean keepUsername = false;
	private String launcherVersion = Authenticator.DEFAULT_LAUNCHER_VERSION;
	private String lwjglDir = "";
	private boolean maximized = false;
	private String nativeDir = "";
	private boolean noExit = false;
	private boolean noFrame = false;
	private float opacity = 1f;
	private List<String> options = new ArrayList<String>();
	private String optionsFileFrom = "";
	private String parentDir = System.getProperty("user.home");
	private String password = "";
	private String port = "25565";
	private boolean printDump = false;
	private boolean printHelp = false;
	private boolean printVersion = false;
	private boolean saveLastLogin = false;
	private String server = null;
	private String sessionId = "0";
	private String texturepack = "";
	private String title = "Minecraft";
	private boolean useLastLogin = false;
	private String username = "Username";
	private int width = 800;
	private int x = -1;
	private int y = -1;
	
	public Parameters(String[] arguments) {
		for (String arg : arguments) {
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
				printVersion = true;
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
				printDump = true;
			} else if (arg.equals("--no-exit")) {
				noExit = true;
			} else if (arg.equals("--help")) {
				printHelp = true;
			} else {
				printHelp = true;
			}
		}
		
		// Check if we were provided with a path, otherwise fall back to the
		// defaults.
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
		
		// Sanity checks
		if (height <= 0) {
			height = 600;
		}
		if (width <= 0) {
			width = 800;
		}
	}
	
	public List<String> getAdditionalJars() {
		return additionalJars;
	}
	
	public String getAppletToLoad() {
		return appletToLoad;
	}
	
	public String getAuthenticationAddress() {
		return authenticationAddress;
	}
	
	public AuthenticationFailureBehavior getAuthenticationFailureBehavior() {
		return authenticationFailureBehavior;
	}
	
	public String getBlendJarName() {
		return blendJarName;
	}
	
	public List<String> getBlendWith() {
		return blendWith;
	}
	
	public int getHeight() {
		return height;
	}
	
	public String getJar() {
		return jar;
	}
	
	public String getJarDir() {
		return jarDir;
	}
	
	public int getKeepAliveTick() {
		return keepAliveTick;
	}
	
	public String getLauncherVersion() {
		return launcherVersion;
	}
	
	public String getLwjglDir() {
		return lwjglDir;
	}
	
	public String getNativeDir() {
		return nativeDir;
	}
	
	public float getOpacity() {
		return opacity;
	}
	
	public List<String> getOptions() {
		return options;
	}
	
	public String getOptionsFileFrom() {
		return optionsFileFrom;
	}
	
	public String getParentDir() {
		return parentDir;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getPort() {
		return port;
	}
	
	public String getServer() {
		return server;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public String getTexturepack() {
		return texturepack;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public boolean isAlwaysOnTop() {
		return alwaysOnTop;
	}
	
	public boolean isAuthenticate() {
		return authenticate;
	}
	
	public boolean isBlendKeepManifest() {
		return blendKeepManifest;
	}
	
	public boolean isDemo() {
		return demo;
	}
	
	public boolean isExitRequested() {
		return exitRequested;
	}
	
	public boolean isFullscreen() {
		return fullscreen;
	}
	
	public boolean isKeepUsername() {
		return keepUsername;
	}
	
	public boolean isMaximized() {
		return maximized;
	}
	
	public boolean isNoExit() {
		return noExit;
	}
	
	public boolean isNoFrame() {
		return noFrame;
	}
	
	public boolean isPrintDump() {
		return printDump;
	}
	
	public boolean isPrintHelp() {
		return printHelp;
	}
	
	public boolean isPrintVersion() {
		return printVersion;
	}
	
	public boolean isSaveLastLogin() {
		return saveLastLogin;
	}
	
	public boolean isUseLastLogin() {
		return useLastLogin;
	}
	
	public void setExitRequested(boolean exitRequested) {
		this.exitRequested = exitRequested;
	}
	
	public void setJar(String jar) {
		this.jar = jar;
	}
	
	public void setOptionsFileFrom(String optionsFileFrom) {
		this.optionsFileFrom = optionsFileFrom;
	}
	
	public void setParentDir(String parentDir) {
		this.parentDir = parentDir;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String toString() {
		StringBuilder value = new StringBuilder();
		value.append("jarDir (exists: ").append(new File(jarDir).exists()).append("): ").append(jarDir);
		value.append("jar (exists: ").append(new File(jar).exists()).append("): ").append(jar);
		value.append("lwjglDir (exists: ").append(new File(lwjglDir).exists()).append("): ").append(lwjglDir);
		value.append("password: ").append(password);
		value.append("nativeDir (exists: ").append(new File(nativeDir).exists()).append("): ").append(nativeDir);
		value.append("additionalJars:");
		for (String additionalJar : additionalJars) {
			value.append("    ").append(additionalJar);
		}
		value.append("noFrame: ").append(noFrame);
		value.append("optionsFileFrom (exists: ").append(new File(optionsFileFrom).exists()).append("): ").append(optionsFileFrom);
		value.append("options:");
		for (String option : options) {
			value.append("    ").append(option);
		}
		value.append("demo: ").append(demo);
		value.append("parentDir (exists: ").append(new File(parentDir).exists()).append("): ").append(parentDir);
		value.append("applet: ").append(appletToLoad);
		value.append("blendWith: ");
		for (String file : blendWith) {
			value.append("	(exists: ").append(new File(file).exists()).append("): ").append(file);
		}
		value.append("blendJarName: ").append(blendJarName);
		value.append("blendKeepManifest: ").append(blendKeepManifest);
		value.append("port: ").append(port);
		value.append("server: ").append(server);
		value.append("authenticate: ").append(authenticate);
		value.append("authenticationFailureBehavior: ").append(authenticationFailureBehavior);
		value.append("keepAliveTick: ").append(keepAliveTick);
		value.append("launcherVersion: ").append(launcherVersion);
		value.append("authenticationAddress: ").append(authenticationAddress);
		value.append("username: ").append(username);
		value.append("useLastLogin: ").append(useLastLogin);
		value.append("saveLastLogin: ").append(saveLastLogin);
		value.append("keepUsername: ").append(keepUsername);
		value.append("texturepack: ").append(texturepack);
		value.append("maximized: ").append(maximized);
		value.append("width: ").append(width);
		value.append("height: ").append(height);
		value.append("x: ").append(x);
		value.append("y: ").append(y);
		value.append("title: ").append(title);
		value.append("alwaysOnTop: ").append(alwaysOnTop);
		value.append("fullscreen: ").append(fullscreen);
		value.append("opacity: ").append(opacity);
		return value.toString();
	}
}
