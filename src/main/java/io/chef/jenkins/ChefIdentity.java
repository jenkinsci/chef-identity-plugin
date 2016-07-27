/* The MIT License (MIT)
 *
 * Copyright (c) 2014 Tyler Fitch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.chef.jenkins;

import hudson.util.Scrambler;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a Chef Identity.
 *
 * @author tfitch
 */
public class ChefIdentity implements Serializable {
	public static final String SHELL_SCRIPT_FILE = "install-chef-cookbook.sh";
	public static final String REMOTE_HOST_CLIENT_RB_FILE = "remote-host-client.rb.conf";
	private static final Logger log = Logger.getLogger(ChefIdentity.class.getName());

	private final String idName;
	private Secret pemKey;
	private Secret knifeRb;
	private final boolean convertedSecret;
	private String nodeJson;
	private String runList;
	private String remoteHost;
	private String remoteAccount;
	private String remoteHostClientRb;

	public ChefIdentity() {
		this.idName = null;
		this.pemKey = null;
		this.knifeRb = null;
		this.nodeJson =  null;
		this.remoteHost = null;
		this.remoteAccount = null;
		this.runList = null;
		this.convertedSecret = false;
		this.remoteHostClientRb = null;
	}

	@DataBoundConstructor
	public ChefIdentity(String idName, String pemKey, String knifeRb, String nodeJson, String runList, String remoteHost, String remoteAccount, String remoteHostClientRb) {
		this.idName = idName;
		this.nodeJson = nodeJson;
		this.runList = runList;
		this.remoteHost = remoteHost;
		this.remoteAccount = remoteAccount;
		this.remoteHostClientRb = remoteHostClientRb;
		if (this.pemKey == null) this.pemKey = Secret.fromString(pemKey);
		if (this.knifeRb == null) this.knifeRb = Secret.fromString(knifeRb);
		this.convertedSecret = true;
	}

	public String getIdName() {
		return idName;
	}

	public String getPemKey() {
		if (convertedSecret) {
			return Secret.toString(pemKey);
		} else {
			return Scrambler.descramble(pemKey.getPlainText());
		}
	}

	public String getKnifeRb() {
		if (convertedSecret) {
			return Secret.toString(knifeRb);
		} else {
			return Scrambler.descramble(knifeRb.getPlainText());
		}
	}

	public String getNodeJson() {
		return nodeJson;
	}

	public String getRunList() {
		return runList;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public String getRemoteAccount() {
		return remoteAccount;
	}

	public String getRemoteHostClientRb() {
		return remoteHostClientRb;
	}
}