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

/**
 * Defines the behavior what happens if authentication failed.
 */
public enum AuthenticationFailureBehavior {
	
	ALERT_BREAK {
		
		@Override
		public boolean isAlert() {
			return true;
		}
		
		@Override
		public boolean isBreak() {
			return true;
		}
	},
	ALERT_CONTINUE {
		
		@Override
		public boolean isAlert() {
			return true;
		}
		
		@Override
		public boolean isBreak() {
			return false;
		}
	},
	SILENT_BREAK {
		
		@Override
		public boolean isAlert() {
			return false;
		}
		
		@Override
		public boolean isBreak() {
			return true;
		}
	},
	SILENT_CONTINUE {
		
		@Override
		public boolean isAlert() {
			return false;
		}
		
		@Override
		public boolean isBreak() {
			return false;
		}
	};
	
	/**
	 * Returns true if the current value includes alerting the user.
	 * 
	 * @return true if we should alert the user
	 */
	public abstract boolean isAlert();
	
	/**
	 * Returns true if the current value includes stopping whatever we were
	 * doing.
	 * 
	 * @return true if we should break
	 */
	public abstract boolean isBreak();
}
