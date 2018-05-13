/**
 * Powerunit - A JDK1.8 test framework
 * Copyright (C) 2014 Mathieu Boretti.
 *
 * This file is part of Powerunit
 *
 * Powerunit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Powerunit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Powerunit. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.powerunit.extensions.matchers.provideprocessor.fields.lang;

public interface BuilderDeclaration extends BuilderShortCutDeclaration{
	BuilderJavadoc withExplicitDeclaration(String declaration);

	BuilderJavadoc withGenericDeclaration(String generic, String postFix, String arguments);
	
	default BuilderJavadoc withSuffixDeclaration(String postfix) {
		return withDeclaration(postfix,"");
	}

	default BuilderJavadoc withDeclaration(String postFix, String arguments) {
		return withGenericDeclaration("", postFix, arguments);
	}

	default BuilderJavadoc withDeclaration(String arguments) {
		return withDeclaration("", arguments);
	}

}
