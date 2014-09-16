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
	private static final Logger log = Logger.getLogger(ChefIdentity.class.getName());

	private final String idName;
	private final String pemKey;
	private final String knifeRb;

	@DataBoundConstructor
	public ChefIdentity(String idName, String pemKey, String knifeRb) {
		this.idName = idName;
		this.pemKey = Scrambler.scramble(pemKey);
		this.knifeRb = Scrambler.scramble(knifeRb);
	}

	public String getIdName() {
		return idName;
	}

	public String getPemKey() {
		return Scrambler.descramble(pemKey);
	}

	public String getKnifeRb() {
		return Scrambler.descramble(knifeRb);
	}
}