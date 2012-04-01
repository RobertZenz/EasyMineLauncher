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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads, parses and saves the options.txt-file.
 * @author Robert 'Bobby' Zenz
 */
public class OptionsFile {

	private static final String SEPARATOR = ":";
	private File file;
	List<String> keys = new ArrayList<String>();
	List<String> values = new ArrayList<String>();

	public OptionsFile(String pathOrFile) {
		this.file = new File(pathOrFile);
		if (this.file.isDirectory()) {
			this.file = new File(pathOrFile, "options.txt");
		}
	}

	public boolean read() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));

			String line;
			while ((line = reader.readLine()) != null) {
				String[] keyValue = line.split(SEPARATOR);
				keys.add(keyValue[0]);
				if (keyValue.length > 1) {
					values.add(keyValue[1]);
				} else {
					values.add("");
				}
			}

			reader.close();
			return true;
		} catch (IOException ex) {
			System.err.println(ex);
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				System.err.println(ex);
			}
		}

		return false;
	}

	public void setTexturePack(String texturepackFilename) {
		setOption("skin", texturepackFilename);
	}

	public boolean write() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));

			for (int idx = 0; idx < keys.size(); idx++) {
				writer.write(keys.get(idx) + SEPARATOR + values.get(idx));
				writer.newLine();
			}

			writer.close();
			return true;
		} catch (IOException ex) {
			System.err.println(ex);
		} finally {
			try {
				writer.close();
			} catch (IOException ex) {
				System.err.println(ex);
			}
		}

		return false;
	}

	private void setOption(String key, String value) {
		values.set(keys.indexOf(key), value);
	}
}
