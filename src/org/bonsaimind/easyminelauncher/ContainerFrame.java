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
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * The main frame which will contain our applet.
 */
public class ContainerFrame extends Frame {

	private Applet containerApplet;

	public ContainerFrame(String title) throws HeadlessException {
		super(title);

		addWindowListener(new WindowListener() {

			public void windowOpened(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
				Applet container = ContainerFrame.this.containerApplet;
				if (container != null) {
					container.stop();
					container.destroy();
				}
				
				// Best way to get rid of a hanging window.
				System.exit(0);
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowActivated(WindowEvent e) {
			}

			public void windowDeactivated(WindowEvent e) {
			}
		});

		setLayout(new BorderLayout());
	}

	/**
	 * Get the ContainerApplet.
	 * @return The ContainerApplet.
	 */
	public Applet getContainerApplet() {
		return containerApplet;
	}

	/**
	 * Set the ContainerApplet.
	 * @param container The ContainerApplet.
	 */
	public void setContainerApplet(ContainerApplet container) {
		this.containerApplet = container;
		add("Center", container);
	}
}
