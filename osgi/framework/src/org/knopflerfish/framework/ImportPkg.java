/*
 * Copyright (c) 2005, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.knopflerfish.framework;

import java.util.*;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;


/**
 * Data structure for import package definitions.
 *
 * @author Jan Stein
 */
class ImportPkg {
  final String name;
  final BundleImpl bundle;
  final String resolution;
  final String bundleSymbolicName;
  final VersionRange packageRange;
  final VersionRange bundleRange;
  final Map attributes;

  // Link to pkg entry
  Pkg pkg = null;

  // Link to exporter
  ExportPkg provider = null;

  /**
   * Create an import package entry.
   */
  ImportPkg(Map tokens, BundleImpl b) {
    this.bundle = b;
    this.name = (String)tokens.remove("key");
    String res = (String)tokens.remove(Constants.RESOLUTION_DIRECTIVE);
    if (res != null) {
      if (Constants.RESOLUTION_OPTIONAL.equals(res)) {
	this.resolution = Constants.RESOLUTION_OPTIONAL;
      }  else if (Constants.RESOLUTION_MANDATORY.equals(res)) {
	this.resolution = Constants.RESOLUTION_MANDATORY;
      } else {
	throw new IllegalArgumentException("Directive " + Constants.RESOLUTION_DIRECTIVE +
					   ", unexpected value: " + res);
      }
    } else {
      this.resolution = Constants.RESOLUTION_MANDATORY;
    }
    this.bundleSymbolicName = (String)tokens.remove(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE);
    String versionStr = (String)tokens.remove(Constants.VERSION_ATTRIBUTE);
    String specVersionStr = (String)tokens.remove(Constants.PACKAGE_SPECIFICATION_VERSION);
    if (specVersionStr != null) {
      this.packageRange = new VersionRange(specVersionStr);
    } else {
      this.packageRange = new VersionRange(versionStr);
    }
    this.bundleRange = new VersionRange((String)tokens.remove(Constants.BUNDLE_VERSION_ATTRIBUTE));
    this.attributes = tokens;
  }


  /**
   * Create an import package entry.
   */
  ImportPkg(String name, BundleImpl b) {
    this.name = name;
    this.bundle = b;
    this.resolution = Constants.RESOLUTION_MANDATORY;
    this.bundleSymbolicName = null;
    this.packageRange = new VersionRange(null);
    this.bundleRange = new VersionRange(null);
    this.attributes = null;
  }


  /**
   * Create an import package entry.
   */
  ImportPkg(ExportPkg p) {
    this.name = p.name;
    this.bundle = p.bundle;
    this.resolution = Constants.RESOLUTION_MANDATORY;;
    this.bundleSymbolicName = null;
    this.packageRange = new VersionRange(p.version.toString());
    this.bundleRange = new VersionRange(null);
    this.attributes = p.attributes;
  }


  /**
   * Package name equal.
   *
   * @param other Package entry to compare to.
   * @return true if equal, otherwise false.
   */
  boolean packageNameEqual(ImportPkg other) {
    return name.equals(other.name);
  }


  /**
   * Version compare object to another ImportPkg.
   *
   * @param obj Version to compare to.
   * @return Return 0 if equals, negative if this object is less than obj
   *         and positive if this object is larger then obj.
   * @exception ClassCastException if object is not a ImportPkg object.
   */
  public int compareVersion(Object obj) throws ClassCastException {
    ImportPkg o = (ImportPkg)obj;
    return packageRange.compareTo(o.packageRange);
  }


  /**
   * Check if version fullfills import package constraints.
   *
   * @param ver Version to compare to.
   * @return Return 0 if equals, negative if this object is less than obj
   *         and positive if this object is larger then obj.
   * @exception ClassCastException if object is not a ImportPkg object.
   */
  public boolean okPackageVersion(Version ver) {
    return packageRange.withinRange(ver);
  }


  /**
   * String describing package name and specification version, if specified.
   *
   * @return String.
   */
  public String pkgString() {
    // NYI
    if (packageRange.isSpecified()) {
      return name + ";" + Constants.VERSION_ATTRIBUTE + "=" + packageRange;
    } else {
      return name;
    }
  }


  /**
   * String describing this object.
   *
   * @return String.
   */
  public String toString() {
    return pkgString() + "(" + bundle + ")";
  }


  /**
   * Check if object is equal to this object.
   *
   * @param obj Package entry to compare to.
   * @return true if equal, otherwise false.
   */
  public boolean equals(Object obj) throws ClassCastException {
    throw new RuntimeException("NYI");
  }


  /**
   * Hash code for this package entry.
   *
   * @return int value.
   */
  public int hashCode() {
    return name.hashCode() + bundle.hashCode();
  }

}