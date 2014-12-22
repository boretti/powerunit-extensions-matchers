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
package ch.powerunit.extensions.matchers.factoryprocessor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.hamcrest.Factory;

@SupportedAnnotationTypes({ "org.hamcrest.Factory" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FactoryAnnotationsProcessor extends AbstractProcessor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
	 * javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Elements elementsUtils = processingEnv.getElementUtils();
		Filer filerUtils = processingEnv.getFiler();
		Types typesUtils = processingEnv.getTypeUtils();
		Messager messageUtils = processingEnv.getMessager();
		TypeElement factoryAnnotationTE = elementsUtils
				.getTypeElement("org.hamcrest.Factory");
		String targets = processingEnv.getOptions().get(
				FactoryAnnotationsProcessor.class.getName() + ".targets");

		if (!roundEnv.processingOver()) {
			if (targets == null || targets.trim().equals("")) {
				messageUtils.printMessage(
						Kind.MANDATORY_WARNING,
						"The parameter `"
								+ FactoryAnnotationsProcessor.class.getName()
								+ ".targets` is missing, please use it.");
				return false;
			}
			List<String[]> targetClass = new ArrayList<>();
			Map<String, Collection<ExecutableElement>> build = new HashMap<>();
			for (String s : targets.split("\\s*;\\s*")) {
				String l1[] = s.split("\\s*:\\s*");
				build.put(l1[1], new ArrayList<>());
				for (String l2 : l1[0].split("\\s*,\\s*")) {
					targetClass.add(new String[] { l2, l1[1] });
				}
			}
			Set<? extends Element> elements = roundEnv
					.getElementsAnnotatedWith(Factory.class);
			for (Element e : elements) {
				ExecutableElement ee = e.accept(new FactoryElementVisitor(this,
						elementsUtils, filerUtils, typesUtils, messageUtils,
						factoryAnnotationTE), null);
				if (ee != null) {
					for (String regex[] : targetClass) {
						if (ee.getEnclosingElement().asType().toString()
								.matches(regex[0])) {
							build.get(regex[1]).add(ee);
							break;
						}
					}
				}
			}
			for (Map.Entry<String, Collection<ExecutableElement>> target : build
					.entrySet()) {
				String targetName = target.getKey();
				if (handled.contains(targetName)) {
					break;
				}
				handled.add(targetName);
				try {
					JavaFileObject jfo = filerUtils.createSourceFile(
							targetName,
							target.getValue().toArray(
									new ExecutableElement[] {}));
					try (PrintWriter wjfo = new PrintWriter(jfo.openWriter());) {
						String fullName = targetName;
						String pName = fullName.replaceAll("\\.[^.]+$", "");
						String cName = fullName.substring(fullName
								.lastIndexOf('.') + 1);
						wjfo.println("package " + pName + ";");
						wjfo.println();
						wjfo.println("/**");
						wjfo.println(" * Factories ...");
						wjfo.println(" */");
						wjfo.println("@javax.annotation.Generated(\""
								+ FactoryAnnotationsProcessor.class.getName()
								+ "\")");
						wjfo.println("public interface " + cName + " {");
						for (ExecutableElement ee : target.getValue()) {
							wjfo.println("  // " + ee.getSimpleName());
							String doc = elementsUtils.getDocComment(ee);
							if (doc != null) {
								wjfo.println("  /**\n   * "
										+ doc.replaceAll("\n", "\n   * ")
										+ "\n   */");
							}
							wjfo.print("  default ");
							wjfo.print(ee.getReturnType().toString());
							wjfo.print(" ");
							wjfo.print(ee.getSimpleName().toString());
							wjfo.print("(");
							wjfo.print(ee
									.getParameters()
									.stream()
									.map((ve) -> ve.asType().toString() + " "
											+ ve.getSimpleName().toString())
									.collect(Collectors.joining(",")));
							wjfo.println(") {");
							wjfo.print("    return ");
							wjfo.print(elementsUtils.getPackageOf(ee
									.getEnclosingElement()));
							wjfo.print(".");
							wjfo.print(ee.getEnclosingElement().getSimpleName()
									.toString());
							wjfo.print(".");
							wjfo.print(ee.getSimpleName().toString());
							wjfo.print("(");
							wjfo.print(ee.getParameters().stream()
									.map((ve) -> ve.getSimpleName().toString())
									.collect(Collectors.joining(",")));
							wjfo.println(");");
							wjfo.println("  }");
							wjfo.println();
						}
						wjfo.println("}");
					}
				} catch (IOException e) {
					messageUtils
							.printMessage(Kind.ERROR,
									"Unable to create the file containing the target class ");
				}
			}
		}
		return true;
	}

	private Set<String> handled = new HashSet<>();

	AnnotationMirror getFactoryAnnotation(TypeElement factoryAnnotationTE,
			Collection<? extends AnnotationMirror> annotations) {
		for (AnnotationMirror a : annotations) {
			if (a.getAnnotationType().equals(factoryAnnotationTE.asType())) {
				return a;
			}
		}
		return null;
	}
}
