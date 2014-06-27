$Id: README,v 1.3 2003/08/22 10:29:19 dvd Exp $

TeX Hyphenator in Java

Re-implementation of Franklin Mark Liang's hyphenation algorithm
as used in TeX by Donald Knuth

by David Tolpin, currently at dvd@davidashen.net

Package Contents

This package consists of:
- the license, LICENSE;
- a .jar file with compiled code, texhyphj.jar;
- a set of Java sources, located at net/davidashen/*;
- documentation, including this file and javadoc for the API, at doc/*;
- sample hyphenation tables, at etc/hyphen/*;
- a Makefile to build the package from the sources.

The package provides a simple Java API to hyphenate a word using
TeX hyphenation tables.

Installation

Place texhyphj.jar in the CLASSPATH.

Compiling

Edit Makefile as appropriate (paths to java binaries and
compilation options can be changed) and execute make. 

Usage

net/davidashen/text/Hyphenator.java contains 

public static void main(String[] args) 

which both serves as a programming example and provides a
simple command-line interface to test hyphenation patterns and
code lists.

Try the following command:

java net.davidashen.test.Hyphenator hyphenation etc/hyphen/hyphen.tex

Synopsis

net.davidashen.text.Hyphenator h=new net.davidashen.text.Hyphenator();
h.setErrorHandler(new MyErrorHandler());
h.loadTable(new java.io.BufferedInputStream(new java.io.FileInputStream("hyphen.tex")));
String hyphenated_word=h.hyphenate(word);

See auto-generated API documentation for interface details.

Support for TeX hyphenation tables

The module accepts most TeX hyphenation tables with no or little
modification. It handles sections 'patterns' (for hyphenation
patterns) and 'hyphenation' (for exceptions); otherwise, it
ignores everything else.

TeX macro definitions are not supported, and are not intended to
be, since the purpose is to make concise and clear code
available, not to re-implement the TeX parser in Java.

Hexadecimal characters (e.g. ^^ae) and control characters (^^A)
are supported; the former ones are usually used for non-ANSI European
characters. Additionally, \rm macros for accented characters are
translated into UCS codes; that is, \^a is 0xe2, \l is 0x142
etc. See net.davidashen.text.Hyphenator.Scanner.acctab for the
full list. 

Code lists

Certain hyphenation tables use encoding other than ISO-8859-1.
To facilitate translation from that particular encoding to UCS,
a list of codes and their unicode values can be passed to the
hyphenator. See ruhyphal.tex, koicodes.txt  for an example of
a KOI8-R-encoded hyphenation table and a list of codes.

David Tolpin
http://www.davidashen.net/

Copyright Notices

TeXHyphenator-J, a hyphenation library in Java is developed by David Tolpin.
Copyright (C) 2003 David Tolpin
URL: http://www.davidashen.net/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

$Log: README,v $
Revision 1.3  2003/08/22 10:29:19  dvd
*** empty log message ***

Revision 1.2  2003/08/21 08:52:59  dvd
pre-release 1.0

Revision 1.1  2003/08/21 08:32:44  dvd
*** empty log message ***

