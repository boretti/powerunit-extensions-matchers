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
package ch.powerunit.extensions.matchers.provideprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FieldDescription {

	public static enum Type {
		NA, ARRAY, COLLECTION, LIST, SET, OPTIONAL, COMPARABLE, STRING
	}

	@FunctionalInterface
	public interface Generator {
		String generate(String inputClassName, String returnMethod, String prefix);
	}

	private final String fieldAccessor;
	private final String fieldName;
	private final String methodFieldName;
	private final String fieldType;
	private final Type type;
	private final List<Generator> implGenerator;
	private final List<Generator> dslGenerator;

	public FieldDescription(String fieldAccessor, String fieldName, String methodFieldName, String fieldType,
			Type type) {
		this.fieldAccessor = fieldAccessor;
		this.fieldName = fieldName;
		this.methodFieldName = methodFieldName;
		this.fieldType = fieldType;
		this.type = type;
		List<Generator> tmp1 = new ArrayList<>();
		List<Generator> tmp2 = new ArrayList<>();
		tmp1.add(this::getImplementationForDefault);
		tmp2.add(this::getDslForDefault);
		switch (type) {
		case ARRAY:
			tmp1.add(this::getImplementationForArray);
			tmp2.add(this::getDslForArray);
			break;
		case OPTIONAL:
			tmp1.add(this::getImplementationForOptional);
			tmp2.add(this::getDslForOptional);
			break;
		case COMPARABLE:
			tmp1.add(this::getImplementationForComparable);
			tmp2.add(this::getDslForComparable);
			break;
		case STRING:
			tmp1.add(this::getImplementationForComparable);
			tmp2.add(this::getDslForComparable);
			tmp1.add(this::getImplementationForString);
			tmp2.add(this::getDslForString);
			break;
		case COLLECTION:
			tmp1.add(this::getImplementationForIterable);
			tmp2.add(this::getDslForIterable);
			tmp1.add(this::getImplementationForCollection);
			tmp2.add(this::getDslForCollection);
			break;
		case LIST:
			tmp1.add(this::getImplementationForIterable);
			tmp2.add(this::getDslForIterable);
			tmp1.add(this::getImplementationForCollection);
			tmp2.add(this::getDslForCollection);
			break;
		case SET:
			tmp1.add(this::getImplementationForIterable);
			tmp2.add(this::getDslForIterable);
			tmp1.add(this::getImplementationForCollection);
			tmp2.add(this::getDslForCollection);
			break;
		default:
			// Nothing
		}
		implGenerator = Collections.unmodifiableList(tmp1);
		dslGenerator = Collections.unmodifiableList(tmp2);
	}

	private String getImplementationForDefault(String inputClassName, String returnMethod, String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "(org.hamcrest.Matcher<? super "
				+ fieldType + "> matcher) {").append("\n");
		sb.append(prefix).append("  " + fieldName + "= new " + methodFieldName + "Matcher(matcher);").append("\n");
		sb.append(prefix).append("  return this;").append("\n");
		sb.append(prefix).append("}").append("\n");

		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "(" + fieldType + " value) {")
				.append("\n");
		sb.append(prefix).append("  return " + fieldName + "(org.hamcrest.Matchers.is(value));").append("\n");
		sb.append(prefix).append("}").append("\n");

		return sb.toString();
	}

	private String getImplementationForString(String inputClassName, String returnMethod, String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "ContainsString(String other) {")
				.append("\n");
		sb.append(prefix).append("  return " + fieldName + "(org.hamcrest.Matchers.containsString(other));")
				.append("\n");
		sb.append(prefix).append("}").append("\n");

		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "StartsWith(String other) {")
				.append("\n");
		sb.append(prefix).append("  return " + fieldName + "(org.hamcrest.Matchers.startsWith(other));").append("\n");
		sb.append(prefix).append("}").append("\n");

		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "EndsWith(String other) {").append("\n");
		sb.append(prefix).append("  return " + fieldName + "(org.hamcrest.Matchers.endsWith(other));").append("\n");
		sb.append(prefix).append("}").append("\n");

		return sb.toString();
	}

	private String getImplementationForIterable(String inputClassName, String returnMethod, String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "IsEmptyIterable() {").append("\n");
		sb.append(prefix)
				.append("  return " + fieldName + "((org.hamcrest.Matcher)org.hamcrest.Matchers.emptyIterable());")
				.append("\n");
		sb.append(prefix).append("}").append("\n");
		return sb.toString();
	}

	private String getImplementationForArray(String inputClassName, String returnMethod, String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "IsEmpty() {").append("\n");
		sb.append(prefix)
				.append("  return " + fieldName + "((org.hamcrest.Matcher)org.hamcrest.Matchers.emptyArray());")
				.append("\n");
		sb.append(prefix).append("}").append("\n");
		return sb.toString();
	}
	
	private String getImplementationForCollection(String inputClassName, String returnMethod, String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "IsEmpty() {").append("\n");
		sb.append(prefix)
				.append("  return " + fieldName + "((org.hamcrest.Matcher)org.hamcrest.Matchers.empty());")
				.append("\n");
		sb.append(prefix).append("}").append("\n");
		return sb.toString();
	}

	private String getImplementationForOptional(String inputClassName, String returnMethod, String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "IsPresent() {").append("\n");
		sb.append(prefix).append("  " + fieldName + " = " + methodFieldName + "Matcher.isPresent();").append("\n");
		sb.append(prefix).append("  return this;").append("\n");
		sb.append(prefix).append("}").append("\n");

		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "IsNotPresent() {").append("\n");
		sb.append(prefix).append("  " + fieldName + " = " + methodFieldName + "Matcher.isNotPresent();").append("\n");
		sb.append(prefix).append("  return this;").append("\n");
		sb.append(prefix).append("}").append("\n");
		return sb.toString();
	}

	private String getImplementationForComparable(String inputClassName, String returnMethod, String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix)
				.append("public " + returnMethod + " " + fieldName + "ComparesEqualTo(" + fieldType + " value) {")
				.append("\n");
		sb.append(prefix).append("  return " + fieldName + "(org.hamcrest.Matchers.comparesEqualTo(value));")
				.append("\n");
		sb.append(prefix).append("}").append("\n");
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "LessThan(" + fieldType + " value) {")
				.append("\n");
		sb.append(prefix).append("  return " + fieldName + "(org.hamcrest.Matchers.lessThan(value));").append("\n");
		sb.append(prefix).append("}").append("\n");
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix)
				.append("public " + returnMethod + " " + fieldName + "LessThanOrEqualTo(" + fieldType + " value) {")
				.append("\n");
		sb.append(prefix).append("  return " + fieldName + "(org.hamcrest.Matchers.lessThanOrEqualTo(value));")
				.append("\n");
		sb.append(prefix).append("}").append("\n");
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix).append("public " + returnMethod + " " + fieldName + "GreaterThan(" + fieldType + " value) {")
				.append("\n");
		sb.append(prefix).append("  return " + fieldName + "(org.hamcrest.Matchers.greaterThan(value));").append("\n");
		sb.append(prefix).append("}").append("\n");
		sb.append(prefix).append("@Override").append("\n");
		sb.append(prefix)
				.append("public " + returnMethod + " " + fieldName + "GreaterThanOrEqualTo(" + fieldType + " value) {")
				.append("\n");
		sb.append(prefix).append("  return " + fieldName + "(org.hamcrest.Matchers.greaterThanOrEqualTo(value));")
				.append("\n");
		sb.append(prefix).append("}").append("\n");
		return sb.toString();
	}

	public String getImplementationInterface(String inputClassName, String returnMethod, String prefix) {
		return implGenerator.stream().map(g -> g.generate(inputClassName, returnMethod, prefix))
				.collect(Collectors.joining("\n"));
	}

	public String getDslForDefault(String inputClassName, String returnMethod, String prefix) {
		String linkToAccessor = "{@link " + inputClassName + "#" + getFieldAccessor()
				+ " This field is accessed by using this approach}.";
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName + ".").append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @param matcher a Matcher on the field.").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName)
				.append("(org.hamcrest.Matcher<? super " + fieldType + "> matcher);").append("\n");

		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName + ".").append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix)
				.append(" * @param value an expected value for the field, which will be compared using the is matcher.")
				.append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" * @see org.hamcrest.Matchers#is(java.lang.Object)").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("(" + fieldType + " value);").append("\n");

		return sb.toString();
	}

	private String getDslForString(String inputClassName, String returnMethod, String prefix) {
		String linkToAccessor = "{@link " + inputClassName + "#" + getFieldAccessor()
				+ " This field is accessed by using this approach}.";
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix)
				.append(" * Add a validation on the field " + fieldName + " that the string contains another one.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @param other the string is contains in the other one.").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" * @see org.hamcrest.Matchers#containsString(java.lang.String)").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("ContainsString(String other);").append("\n");

		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix)
				.append(" * Add a validation on the field " + fieldName + " that the string contains another one.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @param other the string is contains in the other one.").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" * @see org.hamcrest.Matchers#startsWith(java.lang.String)").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("StartsWith(String other);").append("\n");

		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix)
				.append(" * Add a validation on the field " + fieldName + " that the string contains another one.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @param other the string is contains in the other one.").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" * @see org.hamcrest.Matchers#endsWith(java.lang.String)").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("EndsWith(String other);").append("\n");

		return sb.toString();
	}

	private String getDslForIterable(String inputClassName, String returnMethod, String prefix) {
		String linkToAccessor = "{@link " + inputClassName + "#" + getFieldAccessor()
				+ " This field is accessed by using this approach}.";
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName + " that the iterable is empty.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("IsEmptyIterable();").append("\n");

		return sb.toString();
	}

	private String getDslForArray(String inputClassName, String returnMethod, String prefix) {
		String linkToAccessor = "{@link " + inputClassName + "#" + getFieldAccessor()
				+ " This field is accessed by using this approach}.";
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName + " that the array is empty.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("IsEmpty();").append("\n");

		return sb.toString();
	}
	
	private String getDslForCollection(String inputClassName, String returnMethod, String prefix) {
		String linkToAccessor = "{@link " + inputClassName + "#" + getFieldAccessor()
				+ " This field is accessed by using this approach}.";
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName + " that the collection is empty.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("IsEmpty();").append("\n");

		return sb.toString();
	}

	private String getDslForOptional(String inputClassName, String returnMethod, String prefix) {
		String linkToAccessor = "{@link " + inputClassName + "#" + getFieldAccessor()
				+ " This field is accessed by using this approach}.";
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName + " with a present optional.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("IsPresent();").append("\n");

		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName + " with a not present optional.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("IsNotPresent();").append("\n");

		return sb.toString();
	}

	private String getDslForComparable(String inputClassName, String returnMethod, String prefix) {
		String linkToAccessor = "{@link " + inputClassName + "#" + getFieldAccessor()
				+ " This field is accessed by using this approach}.";
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append(" /**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName
				+ " that this field is equals to another one, using the compareTo method.").append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @param value the value to compare with").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" * @see org.hamcrest.Matchers#comparesEqualTo(java.lang.Comparable) ").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("ComparesEqualTo(" + fieldType + " value);")
				.append("\n");

		sb.append(prefix).append(" /**").append("\n");
		sb.append(prefix).append(
				" * Add a validation on the field " + fieldName + " that this field is less than another value.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @param value the value to compare with").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" * @see org.hamcrest.Matchers#lessThan(java.lang.Comparable) ").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("LessThan(" + fieldType + " value);")
				.append("\n");

		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName
				+ " that this field is less or equal than another value.").append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @param value the value to compare with").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" * @see org.hamcrest.Matchers#lessThanOrEqualTo(java.lang.Comparable) ").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("LessThanOrEqualTo(" + fieldType + " value);")
				.append("\n");

		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(
				" * Add a validation on the field " + fieldName + " that this field is greater than another value.")
				.append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @param value the value to compare with").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" * @see org.hamcrest.Matchers#greaterThan(java.lang.Comparable) ").append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName).append("GreaterThan(" + fieldType + " value);")
				.append("\n");

		sb.append(prefix).append("/**").append("\n");
		sb.append(prefix).append(" * Add a validation on the field " + fieldName
				+ " that this field is greater or equal than another value.").append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * ").append(linkToAccessor).append("\n");
		sb.append(prefix).append(" *").append("\n");
		sb.append(prefix).append(" * @param value the value to compare with").append("\n");
		sb.append(prefix).append(" * @return the DSL to continue the construction of the matcher.").append("\n");
		sb.append(prefix).append(" * @see org.hamcrest.Matchers#greaterThanOrEqualTo(java.lang.Comparable) ")
				.append("\n");
		sb.append(prefix).append(" */").append("\n");
		sb.append(prefix).append(returnMethod).append(fieldName)
				.append("GreaterThanOrEqualTo(" + fieldType + " value);").append("\n");

		return sb.toString();
	}

	public String getDslInterface(String inputClassName, String returnMethod, String prefix) {
		return dslGenerator.stream().map(g -> g.generate(inputClassName, returnMethod, prefix))
				.collect(Collectors.joining("\n"));
	}

	public String getFieldAccessor() {
		return fieldAccessor;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getMethodFieldName() {
		return methodFieldName;
	}

	public String getFieldType() {
		return fieldType;
	}

	public Type getType() {
		return type;
	}

}
