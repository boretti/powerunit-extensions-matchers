package ch.powerunit.extensions.matchers.provideprocessor;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import ch.powerunit.extensions.matchers.ProvideMatchers;

public class ProvideMatchersAnnotatedElementMirror {

	private final TypeElement typeElementForClassAnnotatedWithProvideMatcher;
	private final Elements elementsUtils;
	private final Filer filerUtils;
	private final Types typesUtils;
	private final Messager messageUtils;
	private final TypeElement objectTE;
	private final Predicate<Element> isInSameRound;
	private final String fullyQualifiedNameOfClassAnnotatedWithProvideMatcher;
	private final String packageNameOfGeneratedClass;
	private final String fullyQualifiedNameOfGeneratedClass;
	private final String simpleNameOfGeneratedClass;
	private final String simpleNameOfClassAnnotatedWithProvideMatcher;
	private final String methodShortClassName;
	private final boolean hasParent;
	private final boolean hasParentInSameRound;
	private final String generic;
	private final String fullGeneric;
	private final String comments;
	private final String paramJavadoc;
	private final String genericParent;
	private final String genericNoParent;
	private final String fullGenericParent;
	private final String fullGenericNoParent;
	private final String defaultReturnMethod;
	private final String fullyQualifiedNameOfSuperClassOfClassAnnotatedWithProvideMatcher;
	private final String simpleNameOfGeneratedInterfaceMatcher;
	private final String simpleNameOfGeneratedImplementationMatcher;
	private final TypeElement typeElementForSuperClassOfClassAnnotatedWithProvideMatcher;
	private final Function<String, ProvideMatchersAnnotatedElementMirror> findMirrorForTypeName;
	private final String genericForChaining;

	public ProvideMatchersAnnotatedElementMirror(TypeElement typeElement, Elements elementsUtils, Filer filerUtils,
			Types typesUtils, Messager messageUtils, Predicate<Element> isInSameRound,
			Function<String, ProvideMatchersAnnotatedElementMirror> findMirrorForTypeName) {
		this.typeElementForClassAnnotatedWithProvideMatcher = typeElement;
		this.elementsUtils = elementsUtils;
		this.filerUtils = filerUtils;
		this.typesUtils = typesUtils;
		this.messageUtils = messageUtils;
		this.isInSameRound = isInSameRound;
		this.objectTE = elementsUtils.getTypeElement("java.lang.Object");
		this.fullyQualifiedNameOfClassAnnotatedWithProvideMatcher = typeElement.getQualifiedName().toString();
		String tpackageName = elementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
		String toutputClassName = fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + "Matchers";
		String tsimpleNameOfGeneratedClass = typeElement.getSimpleName().toString() + "Matchers";
		ProvideMatchers pm = typeElement.getAnnotation(ProvideMatchers.class);
		this.comments = pm.comments();
		if (!"".equals(pm.matchersClassName())) {
			toutputClassName = toutputClassName.replaceAll(tsimpleNameOfGeneratedClass + "$", pm.matchersClassName());
			tsimpleNameOfGeneratedClass = pm.matchersClassName();
		}
		this.simpleNameOfGeneratedClass = tsimpleNameOfGeneratedClass;
		if (!"".equals(pm.matchersPackageName())) {
			toutputClassName = toutputClassName.replaceAll("^" + tpackageName, pm.matchersPackageName());
			tpackageName = pm.matchersPackageName();
		}
		this.fullyQualifiedNameOfGeneratedClass = toutputClassName;
		this.packageNameOfGeneratedClass = tpackageName;
		this.simpleNameOfClassAnnotatedWithProvideMatcher = typeElement.getSimpleName().toString();
		this.methodShortClassName = simpleNameOfClassAnnotatedWithProvideMatcher.substring(0, 1).toLowerCase()
				+ simpleNameOfClassAnnotatedWithProvideMatcher.substring(1);
		this.hasParent = !objectTE.asType().equals(typeElement.getSuperclass());
		this.hasParentInSameRound = isInSameRound.test(typeElement);
		this.fullyQualifiedNameOfSuperClassOfClassAnnotatedWithProvideMatcher = typeElement.getSuperclass().toString();
		this.typeElementForSuperClassOfClassAnnotatedWithProvideMatcher = (TypeElement) typesUtils
				.asElement(typeElement.getSuperclass());

		if (typeElement.getTypeParameters().size() > 0) {
			this.generic = "<"
					+ typeElement.getTypeParameters().stream().map(t -> t.toString()).collect(Collectors.joining(","))
					+ ">";
			this.fullGeneric = "<" + typeElement.getTypeParameters().stream()
					.map(t -> t.toString() + " extends "
							+ t.getBounds().stream().map(b -> b.toString()).collect(Collectors.joining("&")))
					.collect(Collectors.joining(",")) + ">";
		} else {
			this.generic = "";
			this.fullGeneric = "";
		}
		this.paramJavadoc = extractParamCommentFromJavadoc(elementsUtils.getDocComment(typeElement));
		this.genericParent = getAddParentToGeneric(generic);
		this.genericNoParent = getAddNoParentToGeneric(generic);
		this.fullGenericParent = getAddParentToGeneric(fullGeneric);
		this.fullGenericNoParent = getAddNoParentToGeneric(fullGeneric);
		this.defaultReturnMethod = simpleNameOfClassAnnotatedWithProvideMatcher + "Matcher" + genericParent;
		this.simpleNameOfGeneratedInterfaceMatcher = simpleNameOfClassAnnotatedWithProvideMatcher + "Matcher";
		this.simpleNameOfGeneratedImplementationMatcher = simpleNameOfClassAnnotatedWithProvideMatcher + "MatcherImpl";
		this.findMirrorForTypeName = findMirrorForTypeName;
		this.genericForChaining = genericParent.replaceAll("^<_PARENT", "<" + fullyQualifiedNameOfGeneratedClass + "."
				+ simpleNameOfGeneratedInterfaceMatcher + genericNoParent);
	}

	public String process() {
		StringBuilder factories = new StringBuilder();

		try {
			messageUtils.printMessage(Kind.NOTE,
					"The class `" + fullyQualifiedNameOfGeneratedClass + "` will be generated as a Matchers class.",
					typeElementForClassAnnotatedWithProvideMatcher);
			JavaFileObject jfo = filerUtils.createSourceFile(fullyQualifiedNameOfGeneratedClass,
					typeElementForClassAnnotatedWithProvideMatcher);
			try (PrintWriter wjfo = new PrintWriter(jfo.openWriter());) {
				wjfo.println("package " + packageNameOfGeneratedClass + ";");
				wjfo.println();
				wjfo.println("/**");
				wjfo.println(" * This class provides matchers for the class {@link "
						+ fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + "}.");
				wjfo.println(" * ");
				wjfo.println(" * @see " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher
						+ " The class for which matchers are provided.");
				wjfo.println(" */");
				wjfo.println("@javax.annotation.Generated(value=\""
						+ ProvidesMatchersAnnotationsProcessor.class.getName() + "\",date=\"" + Instant.now().toString()
						+ "\",comments=" + toJavaSyntax(comments) + ")");
				wjfo.println("public final class " + simpleNameOfGeneratedClass + " {");
				wjfo.println();
				wjfo.println("  private " + simpleNameOfGeneratedClass + "() {}");
				wjfo.println();
				List<FieldDescription> fields = generateAndExtractFieldAndParentPrivateMatcher(wjfo);
				generatePublicInterface(wjfo, fields);
				wjfo.println();
				generatePrivateImplementation(wjfo, fields);

				wjfo.println();

				factories.append(generateDSLStarter(wjfo, fields));
				wjfo.println("}");
			}
		} catch (IOException e1) {
			messageUtils.printMessage(Kind.ERROR, "Unable to create the file containing the target class",
					typeElementForClassAnnotatedWithProvideMatcher);
		}
		return factories.toString();
	}

	private List<FieldDescription> generateAndExtractFieldAndParentPrivateMatcher(PrintWriter wjfo) {
		ProvidesMatchersSubElementVisitor providesMatchersSubElementVisitor = new ProvidesMatchersSubElementVisitor(
				elementsUtils, typesUtils, messageUtils, isInSameRound);
		List<FieldDescription> fields = typeElementForClassAnnotatedWithProvideMatcher.getEnclosedElements().stream()
				.map(ie -> ie.accept(providesMatchersSubElementVisitor, this)).filter(Optional::isPresent)
				.map(t -> t.get()).collect(Collectors.toList());
		wjfo.println(fields.stream().map(f -> f.getMatcherForField("  ")).collect(Collectors.joining("\n")));
		if (hasParent) {
			wjfo.println("  private static class SuperClassMatcher" + fullGeneric
					+ " extends org.hamcrest.FeatureMatcher<" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher
					+ "," + fullyQualifiedNameOfSuperClassOfClassAnnotatedWithProvideMatcher + "> {");
			wjfo.println();
			wjfo.println("    public SuperClassMatcher(org.hamcrest.Matcher<? super "
					+ fullyQualifiedNameOfSuperClassOfClassAnnotatedWithProvideMatcher + "> matcher) {");
			wjfo.println("      super(matcher,\"parent\",\"parent\");");
			wjfo.println("  }");
			wjfo.println();
			wjfo.println("    protected " + fullyQualifiedNameOfSuperClassOfClassAnnotatedWithProvideMatcher
					+ " featureValueOf(" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " actual) {");
			wjfo.println("      return actual;");
			wjfo.println("    }");
			wjfo.println();
			wjfo.println("  }");
			wjfo.println();
			wjfo.println();
		}
		return fields;
	}

	private void generatePublicInterface(PrintWriter wjfo, List<FieldDescription> fields) {

		wjfo.println(generateJavaDoc("  ",
				"DSL interface for matcher on {@link " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
						+ simpleNameOfClassAnnotatedWithProvideMatcher + "} to support the build syntaxic sugar",
				Optional.empty(), Optional.empty(), Optional.empty(), true, false));
		wjfo.println("  public static interface " + simpleNameOfGeneratedInterfaceMatcher + "BuildSyntaxicSugar"
				+ fullGeneric + " extends org.hamcrest.Matcher<" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher
				+ generic + "> {");
		wjfo.println(generateJavaDoc("  ", "Method that return the matcher itself.",
				Optional.of(
						"<b>This method is a syntaxic sugar that end the DSL and make clear that the matcher can't be change anymore.</b>"),
				Optional.empty(), Optional.of("the matcher"), false, false));
		wjfo.println("    default org.hamcrest.Matcher<" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher
				+ generic + "> build() {");
		wjfo.println("      return this;");
		wjfo.println("    }");
		wjfo.println("  }");

		wjfo.println(generateJavaDoc("  ",
				"DSL interface for matcher on {@link " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
						+ simpleNameOfClassAnnotatedWithProvideMatcher + "} to support the end syntaxic sugar",
				Optional.empty(), Optional.empty(), Optional.empty(), true, true));
		wjfo.println("  public static interface " + simpleNameOfGeneratedInterfaceMatcher + "EndSyntaxicSugar"
				+ fullGenericParent + " extends org.hamcrest.Matcher<"
				+ fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + generic + "> {");
		wjfo.println(generateJavaDoc("  ", "Method that return the parent builder",
				Optional.of(
						"<b>This method only works in the contexte of a parent builder. If the real type is Void, then nothing will be returned.</b>"),
				Optional.empty(), Optional.of("the parent builder or null if not applicable"), false, false));
		wjfo.println("    _PARENT end();");
		wjfo.println("  }");

		wjfo.println(generateJavaDoc("  ",
				"DSL interface for matcher on {@link " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
						+ simpleNameOfClassAnnotatedWithProvideMatcher + "}",
				Optional.empty(), Optional.empty(), Optional.empty(), true, true));
		wjfo.println("  public static interface " + simpleNameOfGeneratedInterfaceMatcher + fullGenericParent
				+ " extends org.hamcrest.Matcher<" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + generic
				+ ">," + simpleNameOfGeneratedInterfaceMatcher + "BuildSyntaxicSugar " + generic + ","
				+ simpleNameOfGeneratedInterfaceMatcher + "EndSyntaxicSugar " + genericParent + " {");
		wjfo.println(fields.stream().map(f -> f.getDslInterface("    ")).collect(Collectors.joining("\n")));
		wjfo.println("  }");

	}

	private void generatePrivateImplementation(PrintWriter wjfo, List<FieldDescription> fields) {
		wjfo.println("  /* package protected */ static class " + simpleNameOfGeneratedImplementationMatcher
				+ fullGenericParent + " extends org.hamcrest.TypeSafeDiagnosingMatcher<"
				+ fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + generic + "> implements "
				+ simpleNameOfGeneratedInterfaceMatcher + genericParent + " {");
		fields.stream().map(f -> "    private " + f.getMethodFieldName() + "Matcher " + f.getFieldName() + " = new "
				+ f.getMethodFieldName() + "Matcher(org.hamcrest.Matchers.anything());").forEach(wjfo::println);
		wjfo.println("    private final _PARENT _parentBuilder;");
		if (hasParent) {
			wjfo.println("    private SuperClassMatcher _parent;");
			wjfo.println();
			wjfo.println("    public " + simpleNameOfGeneratedImplementationMatcher + "(org.hamcrest.Matcher<? super "
					+ fullyQualifiedNameOfSuperClassOfClassAnnotatedWithProvideMatcher + "> parent) {");
			wjfo.println("      this._parent=new SuperClassMatcher(parent);");
			wjfo.println("      this._parentBuilder=null;");
			wjfo.println("    }");
			wjfo.println();
			wjfo.println();
			wjfo.println("    public " + simpleNameOfGeneratedImplementationMatcher + "(org.hamcrest.Matcher<? super "
					+ fullyQualifiedNameOfSuperClassOfClassAnnotatedWithProvideMatcher
					+ "> parent,_PARENT parentBuilder) {");
			wjfo.println("      this._parent=new SuperClassMatcher(parent);");
			wjfo.println("      this._parentBuilder=parentBuilder;");
			wjfo.println("    }");
			wjfo.println();
		} else {
			wjfo.println();
			wjfo.println("    public " + simpleNameOfClassAnnotatedWithProvideMatcher + "MatcherImpl() {");
			wjfo.println("      this._parentBuilder=null;");
			wjfo.println("    }");
			wjfo.println();
			wjfo.println();
			wjfo.println("    public " + simpleNameOfGeneratedImplementationMatcher + "(_PARENT parentBuilder) {");
			wjfo.println("      this._parentBuilder=parentBuilder;");
			wjfo.println("    }");
			wjfo.println();
		}

		wjfo.println(fields.stream().map(f -> f.getImplementationInterface("    ")).collect(Collectors.joining("\n")));

		wjfo.println("    @Override");
		wjfo.println("    protected boolean matchesSafely(" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher
				+ " actual, org.hamcrest.Description mismatchDescription) {");
		wjfo.println("      boolean result=true;");
		if (hasParent) {
			wjfo.println("      if(!_parent.matches(actual)) {");
			wjfo.println(
					"        mismatchDescription.appendText(\"[\"); _parent.describeMismatch(actual,mismatchDescription); mismatchDescription.appendText(\"]\\n\");");
			wjfo.println("        result=false;");
			wjfo.println("      }");
		}
		for (FieldDescription f : fields) {
			wjfo.println("      if(!" + f.getFieldName() + ".matches(actual)) {");
			wjfo.println("        mismatchDescription.appendText(\"[\"); " + f.getFieldName()
					+ ".describeMismatch(actual,mismatchDescription); mismatchDescription.appendText(\"]\\n\");");
			wjfo.println("        result=false;");
			wjfo.println("      }");
		}
		wjfo.println("      return result;");
		wjfo.println("    }");
		wjfo.println();
		wjfo.println("    @Override");
		wjfo.println("    public void describeTo(org.hamcrest.Description description) {");
		wjfo.println("        description.appendText(\"an instance of "
				+ fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " with\\n\");");
		if (hasParent) {
			wjfo.println("        description.appendText(\"[\").appendDescriptionOf(_parent).appendText(\"]\\n\");");
		}
		fields.stream().map(f -> "        description.appendText(\"[\").appendDescriptionOf(" + f.getFieldName()
				+ ").appendText(\"]\\n\");").forEach(wjfo::println);
		wjfo.println("    }");
		wjfo.println();
		wjfo.println("    @Override");
		wjfo.println("    public _PARENT end() {");
		wjfo.println("      return _parentBuilder;");
		wjfo.println("    }");

		wjfo.println("  }");
	}

	private String generateDSLStarter(PrintWriter wjfo, List<FieldDescription> fields) {
		StringBuilder factories = new StringBuilder();
		factories.append(generateDefaultDSLStarter(wjfo));

		factories.append(generateDefaultForChainingDSLStarter(wjfo));

		if (hasParent) {
			factories.append(generateParentDSLStarter(wjfo));
		}

		wjfo.println();

		if (!hasParent) {
			factories.append(generateNoParentDSLStarter(wjfo, fields));
		}
		if (hasParent && hasParentInSameRound) {
			factories.append(generateParentInSameRoundDSLStarter(wjfo, fields));
		}
		return factories.toString();
	}

	private String generateDefaultDSLStarter(PrintWriter wjfo) {
		StringBuilder factories = new StringBuilder();
		StringBuilder javadoc = new StringBuilder();
		String methodName = fullGeneric + " " + simpleNameOfGeneratedInterfaceMatcher + genericNoParent + " "
				+ methodShortClassName + "With()";

		javadoc.append(generateJavaDoc("  ",
				"Start a DSL matcher for the {@link " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
						+ simpleNameOfClassAnnotatedWithProvideMatcher + "}",
				Optional.of(
						"The returned builder (which is also a Matcher), at this point accepts any object that is a {@link "
								+ fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
								+ simpleNameOfClassAnnotatedWithProvideMatcher + "}."),
				Optional.empty(), Optional.of("the DSL matcher"), true, false));

		wjfo.println(javadoc.toString());
		factories.append(javadoc.toString());

		wjfo.println("  @org.hamcrest.Factory");
		wjfo.println("  public static " + methodName + " {");
		factories.append("  default " + fullGeneric + " " + fullyQualifiedNameOfGeneratedClass + "."
				+ simpleNameOfGeneratedInterfaceMatcher + genericNoParent + " " + methodShortClassName + "With()"
				+ " {").append("\n");
		factories.append("    return " + fullyQualifiedNameOfGeneratedClass + "." + methodShortClassName + "With();")
				.append("\n");
		factories.append("  }").append("\n");
		if (hasParent) {
			wjfo.println("    return new " + simpleNameOfGeneratedImplementationMatcher + genericNoParent
					+ "(org.hamcrest.Matchers.anything());");
		} else {
			wjfo.println("    return new " + simpleNameOfGeneratedImplementationMatcher + genericNoParent + "();");
		}
		wjfo.println("  }");
		return factories.toString();
	}

	private String generateDefaultForChainingDSLStarter(PrintWriter wjfo) {
		StringBuilder factories = new StringBuilder();
		StringBuilder javadoc = new StringBuilder();
		String methodName = fullGenericParent + " " + simpleNameOfGeneratedInterfaceMatcher + genericParent + " "
				+ methodShortClassName + "WithParent(_PARENT parentBuilder)";
		javadoc.append(generateJavaDoc("  ",
				"Start a DSL matcher for the {@link " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
						+ simpleNameOfClassAnnotatedWithProvideMatcher + "}",
				Optional.of(
						"The returned builder (which is also a Matcher), at this point accepts any object that is a {@link "
								+ fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
								+ simpleNameOfClassAnnotatedWithProvideMatcher + "}."),
				Optional.of("parentBuilder the parentBuilder."), Optional.of("the DSL matcher"), true, true));

		wjfo.println(javadoc.toString());

		wjfo.println("  public static " + methodName + " {");
		if (hasParent) {
			wjfo.println("    return new " + simpleNameOfGeneratedImplementationMatcher + genericParent
					+ "(org.hamcrest.Matchers.anything(),parentBuilder);");
		} else {
			wjfo.println("    return new " + simpleNameOfGeneratedImplementationMatcher + genericParent
					+ "(parentBuilder);");
		}
		wjfo.println("  }");
		return factories.toString();
	}

	private String generateParentDSLStarter(PrintWriter wjfo) {
		StringBuilder factories = new StringBuilder();
		StringBuilder javadoc = new StringBuilder();
		javadoc.append(generateJavaDoc("  ",
				"Start a DSL matcher for the {@link " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
						+ simpleNameOfClassAnnotatedWithProvideMatcher + "}",
				Optional.empty(), Optional.of("matcherOnParent the matcher on the parent data."),
				Optional.of("the DSL matcher"), true, false));

		wjfo.println(javadoc.toString());
		factories.append(javadoc.toString());

		wjfo.println("  @org.hamcrest.Factory");
		wjfo.println("  public static " + fullGeneric + " " + simpleNameOfGeneratedInterfaceMatcher + genericNoParent
				+ " " + methodShortClassName + "With(org.hamcrest.Matcher<? super "
				+ fullyQualifiedNameOfSuperClassOfClassAnnotatedWithProvideMatcher + "> matcherOnParent) {");
		wjfo.println("    return new " + simpleNameOfGeneratedImplementationMatcher + genericNoParent
				+ "(matcherOnParent);");
		wjfo.println("  }");

		factories.append("  default " + fullGeneric + " " + fullyQualifiedNameOfGeneratedClass + "."
				+ simpleNameOfGeneratedInterfaceMatcher + genericNoParent + " " + methodShortClassName
				+ "With(org.hamcrest.Matcher<? super "
				+ fullyQualifiedNameOfSuperClassOfClassAnnotatedWithProvideMatcher + "> matcherOnParent)" + " {")
				.append("\n");
		factories.append("    return " + fullyQualifiedNameOfGeneratedClass + "." + methodShortClassName
				+ "With(matcherOnParent);").append("\n");
		factories.append("  }").append("\n");
		return factories.toString();
	}

	private String generateNoParentDSLStarter(PrintWriter wjfo, List<FieldDescription> fields) {
		StringBuilder factories = new StringBuilder();
		StringBuilder javadoc = new StringBuilder();
		javadoc.append(generateJavaDoc("  ",
				"Start a DSL matcher for the {@link " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
						+ simpleNameOfClassAnnotatedWithProvideMatcher + "}",
				Optional.empty(), Optional.of("other the other object to be used as a reference."),
				Optional.of("the DSL matcher"), true, false));
		wjfo.println(javadoc.toString());
		factories.append(javadoc.toString());
		wjfo.println("  @org.hamcrest.Factory");
		wjfo.println("  public static " + fullGeneric + " " + simpleNameOfGeneratedInterfaceMatcher + genericNoParent
				+ " " + methodShortClassName + "WithSameValue(" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher
				+ " " + generic + " other) {");
		wjfo.println("    " + simpleNameOfGeneratedInterfaceMatcher + genericNoParent + " m=new "
				+ simpleNameOfGeneratedImplementationMatcher + genericNoParent + "();");

		fields.stream().map(
				f -> "    m." + f.getFieldName() + "(org.hamcrest.Matchers.is(other." + f.getFieldAccessor() + "));")
				.forEach(wjfo::println);
		wjfo.println("    return m;");
		wjfo.println("  }");

		factories.append("  default " + fullGeneric + " " + fullyQualifiedNameOfGeneratedClass + "."
				+ simpleNameOfGeneratedInterfaceMatcher + genericNoParent + " " + methodShortClassName
				+ "WithSameValue(" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " " + generic + " other)"
				+ " {").append("\n");
		factories.append("    return " + fullyQualifiedNameOfGeneratedClass + "." + methodShortClassName
				+ "WithSameValue(other);").append("\n");
		factories.append("  }").append("\n");
		return factories.toString();
	}

	private String generateParentInSameRoundDSLStarter(PrintWriter wjfo, List<FieldDescription> fields) {
		StringBuilder factories = new StringBuilder();
		ProvideMatchersAnnotatedElementMirror parentMirror = findMirrorForTypeName
				.apply(typeElementForSuperClassOfClassAnnotatedWithProvideMatcher.getQualifiedName().toString());
		factories.append(generateParentInSameRoundSameValueDSLStarter(wjfo, fields, parentMirror));

		if (typeElementForSuperClassOfClassAnnotatedWithProvideMatcher.getTypeParameters().isEmpty()) {
			factories.append(generateParentInSameRoundWithChaningDSLStarter(wjfo, parentMirror));
		}
		return factories.toString();
	}

	private String generateParentInSameRoundSameValueDSLStarter(PrintWriter wjfo, List<FieldDescription> fields,
			ProvideMatchersAnnotatedElementMirror parentMirror) {
		StringBuilder factories = new StringBuilder();
		StringBuilder javadoc = new StringBuilder();
		javadoc.append(generateJavaDoc("  ",
				"Start a DSL matcher for the {@link " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
						+ simpleNameOfClassAnnotatedWithProvideMatcher + "}",
				Optional.empty(), Optional.of("other the other object to be used as a reference."),
				Optional.of("the DSL matcher"), true, false));
		wjfo.println(javadoc.toString());
		factories.append(javadoc.toString());
		wjfo.println("  @org.hamcrest.Factory");
		wjfo.println("  public static " + fullGeneric + " " + simpleNameOfGeneratedInterfaceMatcher + genericNoParent
				+ " " + methodShortClassName + "WithSameValue(" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher
				+ " " + generic + " other) {");
		wjfo.println("    " + simpleNameOfGeneratedInterfaceMatcher + genericNoParent + " m=new "
				+ simpleNameOfGeneratedImplementationMatcher + genericNoParent + "("
				+ parentMirror.fullyQualifiedNameOfGeneratedClass + "." + parentMirror.methodShortClassName
				+ "WithSameValue(other));");

		fields.stream().map(
				f -> "    m." + f.getFieldName() + "(org.hamcrest.Matchers.is(other." + f.getFieldAccessor() + "));")
				.forEach(wjfo::println);
		wjfo.println("    return m;");
		wjfo.println("  }");

		factories.append("  default " + fullGeneric + " " + fullyQualifiedNameOfGeneratedClass + "."
				+ simpleNameOfGeneratedInterfaceMatcher + genericNoParent + " " + methodShortClassName
				+ "WithSameValue(" + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " " + generic + " other)"
				+ " {").append("\n");
		factories.append("    return " + fullyQualifiedNameOfGeneratedClass + "." + methodShortClassName
				+ "WithSameValue(other);").append("\n");
		factories.append("  }").append("\n");
		return factories.toString();
	}

	private String generateParentInSameRoundWithChaningDSLStarter(PrintWriter wjfo,
			ProvideMatchersAnnotatedElementMirror parentMirror) {
		StringBuilder factories = new StringBuilder();
		StringBuilder javadoc = new StringBuilder();
		javadoc.append(generateJavaDoc("  ",
				"Start a DSL matcher for the {@link " + fullyQualifiedNameOfClassAnnotatedWithProvideMatcher + " "
						+ simpleNameOfClassAnnotatedWithProvideMatcher + "}",
				Optional.empty(), Optional.empty(), Optional.of("the DSL matcher"), true, false));

		wjfo.println(javadoc.toString());
		factories.append(javadoc.toString());
		wjfo.println("  @org.hamcrest.Factory");
		wjfo.println("  public static " + fullGeneric + " " + parentMirror.fullyQualifiedNameOfGeneratedClass + "."
				+ parentMirror.simpleNameOfGeneratedInterfaceMatcher + genericForChaining + " " + methodShortClassName
				+ "WithParent() {");
		wjfo.println("    " + simpleNameOfGeneratedImplementationMatcher + genericNoParent + " m=new "
				+ simpleNameOfGeneratedImplementationMatcher + genericNoParent + "(org.hamcrest.Matchers.anything());");

		wjfo.println("    " + parentMirror.fullyQualifiedNameOfGeneratedClass + "."
				+ parentMirror.simpleNameOfGeneratedInterfaceMatcher + " tmp = "
				+ parentMirror.fullyQualifiedNameOfGeneratedClass + "." + parentMirror.methodShortClassName
				+ "WithParent(m);");
		wjfo.println("    m._parent = new SuperClassMatcher(tmp);");
		wjfo.println("    return tmp;");
		wjfo.println("  }");

		factories.append("  default " + fullGeneric + " " + parentMirror.fullyQualifiedNameOfGeneratedClass + "."
				+ parentMirror.simpleNameOfGeneratedInterfaceMatcher + genericForChaining + " " + methodShortClassName
				+ "WithParent()" + " {").append("\n");
		factories.append(
				"    return " + fullyQualifiedNameOfGeneratedClass + "." + methodShortClassName + "WithParent();")
				.append("\n");
		factories.append("  }").append("\n");
		return factories.toString();
	}

	private String generateJavaDoc(String prefix, String description, Optional<String> moreDetails,
			Optional<String> param, Optional<String> returnDescription, boolean withParam, boolean withParent) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * ").append(description).append(".\n");
		moreDetails.ifPresent(
				t -> sb.append(prefix).append(" * <p>\n").append(prefix).append(" * ").append(t).append("\n"));
		param.ifPresent(t -> sb.append(prefix).append(" * @param ").append(t).append("\n"));
		if (withParam) {
			sb.append(prefix).append(paramJavadoc.replaceAll("\\R", "\n" + prefix)).append(" * \n");
		}
		if (withParent) {
			sb.append(prefix)
					.append(" * @param <_PARENT> used to reference, if necessary, a parent for this builder. By default Void is used an indicate no parent builder")
					.append("\n");
		}
		returnDescription.ifPresent(t -> sb.append(prefix).append(" * @return ").append(t).append("\n"));
		sb.append(prefix).append(" */").append("\n");
		return sb.toString();
	}

	private static String toJavaSyntax(String unformatted) {
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		for (char c : unformatted.toCharArray()) {
			sb.append(toJavaSyntax(c));
		}
		sb.append('"');
		return sb.toString();
	}

	private static String toJavaSyntax(char ch) {
		switch (ch) {
		case '"':
			return "\\\"";
		case '\n':
			return "\\n";
		case '\r':
			return "\\r";
		case '\t':
			return "\\t";
		default:
			return "" + ch;
		}
	}

	private static String extractParamCommentFromJavadoc(String docComment) {
		if (docComment == null) {
			return " * \n";
		}
		boolean insideParam = false;
		StringBuilder sb = new StringBuilder();
		sb.append(" * \n");
		for (String line : docComment.split("\\R")) {
			if (insideParam && line.matches("^\\s*@.*$")) {
				insideParam = false;
			}
			if (line.matches("^\\s*@param.*$")) {
				insideParam = true;
			}
			if (insideParam) {
				sb.append(" *" + line).append("\n");
			}
		}
		return sb.toString();
	}

	private static String getAddParentToGeneric(String generic) {
		if ("".equals(generic)) {
			return "<_PARENT>";
		} else {
			return generic.replaceFirst("<", "<_PARENT,");
		}
	}

	private static String getAddNoParentToGeneric(String generic) {
		if ("".equals(generic)) {
			return "<Void>";
		} else {
			return generic.replaceFirst("<", "<Void,");
		}
	}

	public String getFullyQualifiedNameOfClassAnnotatedWithProvideMatcher() {
		return fullyQualifiedNameOfClassAnnotatedWithProvideMatcher;
	}

	public String getDefaultReturnMethod() {
		return defaultReturnMethod;
	}

	public String getFullGeneric() {
		return fullGeneric;
	}

	public String getGeneric() {
		return generic;
	}

}