/**
 *    Copyright 2006-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.model;

import static org.mybatis.generator.internal.util.JavaBeansUtil.*;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

public class ExampleGenerator extends AbstractJavaGenerator {

	public ExampleGenerator(String project) {
		super(project);
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString(
				"Progress.6", table.toString())); //$NON-NLS-1$
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(
				introspectedTable.getExampleType());
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);

		//add suman
		String baseExampleName = "com.github.sumanit.base.BaseExample<"+type.getShortName()+".Criteria>";

		FullyQualifiedJavaType superType = new FullyQualifiedJavaType(baseExampleName);
		topLevelClass.setSuperClass(superType);
		topLevelClass.addImportedType(new FullyQualifiedJavaType("com.github.sumanit.base.BaseExample"));
		//add suman

		// add default constructor
		Method method = new Method(type.getShortName());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		if (context.isJava8Targeted()) {
			method.addBodyLine("oredCriteria = new ArrayList<>();"); //$NON-NLS-1$
		} else {
			method.addBodyLine("oredCriteria = new ArrayList<Criteria>();"); //$NON-NLS-1$
		}

		method.addBodyLine("tableName = \""+introspectedTable.getFullyQualifiedTable().getIntrospectedTableName()+"\";");
		method.addBodyLine("tableAlias = \""+introspectedTable.getFullyQualifiedTable().getAlias()+"\";");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		// add by suman start
		for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
			Field constField = new Field("COLUMN_"+introspectedColumn.getJavaProperty().toUpperCase(),FullyQualifiedJavaType.getStringInstance());
			constField.setStatic(true);
			constField.setFinal(true);
			constField.setVisibility(JavaVisibility.PUBLIC);
			constField.setInitializationString("\""+introspectedColumn.getActualColumnName()+"\"");
			topLevelClass.addField(constField);

			constField = new Field("FIELD_"+introspectedColumn.getJavaProperty().toUpperCase(),FullyQualifiedJavaType.getStringInstance());
			constField.setStatic(true);
			constField.setFinal(true);
			constField.setVisibility(JavaVisibility.PUBLIC);
			constField.setInitializationString("\""+introspectedColumn.getJavaProperty()+"\"");
			topLevelClass.addField(constField);
			IntrospectedColumn introspectedImportColumn = introspectedColumn.getIntrospectedImportColumn();
			if (introspectedImportColumn == null) {
				continue;
			}
			IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
			if(introspectedImportTable.equals(introspectedTable)){
				continue;
			}
			constField = new Field("FIELD_"+new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType()).getShortName().toUpperCase(),FullyQualifiedJavaType.getStringInstance());
			constField.setStatic(true);
			constField.setFinal(true);
			constField.setVisibility(JavaVisibility.PUBLIC);
			constField.setInitializationString("\""+getValidPropertyName(new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType()).getShortName())+"\"");
			topLevelClass.addField(constField);
			method = getCreateOtherExampleColumnsMethod(introspectedImportTable);
			commentGenerator.addGeneralMethodComment(method, introspectedTable);
			topLevelClass.addMethod(method);
			topLevelClass.addMethod(getAndOtherExampleCriteriaMethod(introspectedImportTable));
			topLevelClass.addMethod(getOrOtherExampleCriteriaMethod(introspectedImportTable));
			topLevelClass.addMethod(getAndOtherExampleCriteriaMethodWithCriteria(introspectedImportTable));
		}

		buildSetComplexPropertyMethod(topLevelClass);
		buildWrapMethod(topLevelClass);

		method = new Method("createColumns");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getColumnContainerInstance());
		method.addBodyLine("ColumnContainer columnContainer = (ColumnContainer) columnContainerMap.get(this.tableName);");
		method.addBodyLine("if(columnContainer == null){");
		method.addBodyLine("columnContainer = new ColumnContainer(this.tableName);");
		method.addBodyLine("columnContainerMap.put(this.tableName,columnContainer);");
		method.addBodyLine("}");
		method.addBodyLine("return (ColumnContainer)columnContainer;");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
		topLevelClass.addInnerClass(getColumnContainerClass(topLevelClass));

		// add by suman end



		// add field, getter, setter for orderby clause
		/*Field field = new Field("orderByClause", FullyQualifiedJavaType.getStringInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PROTECTED);
		commentGenerator.addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);

		method = new Method("setOrderByClause"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "orderByClause")); //$NON-NLS-1$
		method.addBodyLine("this.orderByClause = orderByClause;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		method = new Method("getOrderByClause"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getStringInstance());
		method.addBodyLine("return orderByClause;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		// add field, getter, setter for distinct
		field = new Field("distinct", FullyQualifiedJavaType.getBooleanPrimitiveInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PROTECTED);
		commentGenerator.addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);

		method = new Method("setDistinct"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getBooleanPrimitiveInstance(), "distinct")); //$NON-NLS-1$
		method.addBodyLine("this.distinct = distinct;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		method = new Method("isDistinct"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType
				.getBooleanPrimitiveInstance());
		method.addBodyLine("return distinct;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		// add field and methods for the list of ored criteria
		FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(
				"java.util.List<Criteria>"); //$NON-NLS-1$
		field = new Field("oredCriteria", fqjt); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PROTECTED);

		commentGenerator.addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);

		method = new Method("getOredCriteria"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(fqjt);
		method.addBodyLine("return oredCriteria;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		method = new Method("or"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getCriteriaInstance(), "criteria")); //$NON-NLS-1$
		method.addBodyLine("oredCriteria.add(criteria);"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);


		method = new Method("or"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		method.addBodyLine("Criteria criteria = createCriteriaInternal();"); //$NON-NLS-1$
		method.addBodyLine("oredCriteria.add(criteria);"); //$NON-NLS-1$
		method.addBodyLine("return criteria;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		method = new Method("createCriteria"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		method.addBodyLine("Criteria criteria = createCriteriaInternal();"); //$NON-NLS-1$
		method.addBodyLine("if (oredCriteria.size() == 0) {"); //$NON-NLS-1$
		method.addBodyLine("oredCriteria.add(criteria);"); //$NON-NLS-1$
		method.addBodyLine("}"); //$NON-NLS-1$
		method.addBodyLine("return criteria;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
		*/
		// comment by suman  上面的方法将在父类中实现
		method = new Method("createCriteriaInternal"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		method.addBodyLine("Criteria criteria = new Criteria(this.tableName,this.ignoreCase);"); //$NON-NLS-1$
		method.addBodyLine("return criteria;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		/*
		method = new Method("clear"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addBodyLine("oredCriteria.clear();"); //$NON-NLS-1$
		method.addBodyLine("orderByClause = null;"); //$NON-NLS-1$
		method.addBodyLine("distinct = false;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
		*/
		// comment by suman  上面的方法将在父类中实现

		// now generate the inner class that holds the AND conditions
		// topLevelClass.addInnerClass(getGeneratedCriteriaInnerClass(topLevelClass));// comment by suman  上面的方法将在父类中实现

		topLevelClass.addInnerClass(getCriteriaInnerClass(topLevelClass)); // change by suman

		// topLevelClass.addInnerClass(getCriterionInnerClass());// comment by suman  上面的方法将在父类中实现

		List<CompilationUnit> answer = new ArrayList<>();
		if (context.getPlugins().modelExampleClassGenerated(
				topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}

	/*private InnerClass getCriterionInnerClass() {
		InnerClass answer = new InnerClass(new FullyQualifiedJavaType(
				"Criterion")); //$NON-NLS-1$
		answer.setVisibility(JavaVisibility.PUBLIC);
		answer.setStatic(true);
		context.getCommentGenerator().addClassComment(answer,
				introspectedTable);

		Field field = new Field("condition", FullyQualifiedJavaType.getStringInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PRIVATE);
		answer.addField(field);
		answer.addMethod(getGetter(field));

		field = new Field("value", FullyQualifiedJavaType.getObjectInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PRIVATE);
		answer.addField(field);
		answer.addMethod(getGetter(field));

		field = new Field("secondValue", FullyQualifiedJavaType.getObjectInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PRIVATE);
		answer.addField(field);
		answer.addMethod(getGetter(field));

		field = new Field("noValue", FullyQualifiedJavaType.getBooleanPrimitiveInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PRIVATE);
		answer.addField(field);
		answer.addMethod(getGetter(field));

		field = new Field("singleValue", FullyQualifiedJavaType.getBooleanPrimitiveInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PRIVATE);
		answer.addField(field);
		answer.addMethod(getGetter(field));

		field = new Field("betweenValue", FullyQualifiedJavaType.getBooleanPrimitiveInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PRIVATE);
		answer.addField(field);
		answer.addMethod(getGetter(field));

		field = new Field("listValue", FullyQualifiedJavaType.getBooleanPrimitiveInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PRIVATE);
		answer.addField(field);
		answer.addMethod(getGetter(field));

		field = new Field("typeHandler", FullyQualifiedJavaType.getStringInstance()); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PRIVATE);
		answer.addField(field);
		answer.addMethod(getGetter(field));

		Method method = new Method("Criterion"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(true);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addBodyLine("super();"); //$NON-NLS-1$
		method.addBodyLine("this.condition = condition;"); //$NON-NLS-1$
		method.addBodyLine("this.typeHandler = null;"); //$NON-NLS-1$
		method.addBodyLine("this.noValue = true;"); //$NON-NLS-1$
		answer.addMethod(method);

		method = new Method("Criterion"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(true);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "value")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "typeHandler")); //$NON-NLS-1$
		method.addBodyLine("super();"); //$NON-NLS-1$
		method.addBodyLine("this.condition = condition;"); //$NON-NLS-1$
		method.addBodyLine("this.value = value;"); //$NON-NLS-1$
		method.addBodyLine("this.typeHandler = typeHandler;"); //$NON-NLS-1$
		method.addBodyLine("if (value instanceof List<?>) {"); //$NON-NLS-1$
		method.addBodyLine("this.listValue = true;"); //$NON-NLS-1$
		method.addBodyLine("} else {"); //$NON-NLS-1$
		method.addBodyLine("this.singleValue = true;"); //$NON-NLS-1$
		method.addBodyLine("}"); //$NON-NLS-1$
		answer.addMethod(method);

		method = new Method("Criterion"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(true);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "value")); //$NON-NLS-1$
		method.addBodyLine("this(condition, value, null);"); //$NON-NLS-1$
		answer.addMethod(method);

		method = new Method("Criterion"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(true);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "value")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "secondValue")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "typeHandler")); //$NON-NLS-1$
		method.addBodyLine("super();"); //$NON-NLS-1$
		method.addBodyLine("this.condition = condition;"); //$NON-NLS-1$
		method.addBodyLine("this.value = value;"); //$NON-NLS-1$
		method.addBodyLine("this.secondValue = secondValue;"); //$NON-NLS-1$
		method.addBodyLine("this.typeHandler = typeHandler;"); //$NON-NLS-1$
		method.addBodyLine("this.betweenValue = true;"); //$NON-NLS-1$
		answer.addMethod(method);

		method = new Method("Criterion"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(true);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "value")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "secondValue")); //$NON-NLS-1$
		method.addBodyLine("this(condition, value, secondValue, null);"); //$NON-NLS-1$
		answer.addMethod(method);

		return answer;
	}*/

/*
	private InnerClass getCriteriaInnerClass() {
		InnerClass answer = new InnerClass(FullyQualifiedJavaType
				.getCriteriaInstance());

		answer.setVisibility(JavaVisibility.PUBLIC);
		answer.setStatic(true);
		answer.setSuperClass(FullyQualifiedJavaType
				.getGeneratedCriteriaInstance());

		context.getCommentGenerator().addClassComment(answer,
				introspectedTable, true);

		Method method = new Method("Criteria"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(true);
		method.addBodyLine("super();"); //$NON-NLS-1$
		answer.addMethod(method);

		return answer;
	}
*/

	private InnerClass getCriteriaInnerClass(
			TopLevelClass topLevelClass) {
		Field field;

		InnerClass answer = new InnerClass(FullyQualifiedJavaType
				.getCriteriaInstance());
		answer.setSuperClass(FullyQualifiedJavaType
				.getGeneratedCriteriaInstance());

		answer.setVisibility(JavaVisibility.PUBLIC);
		answer.setStatic(true);
		//answer.setAbstract(true);
		context.getCommentGenerator().addClassComment(answer,
				introspectedTable);

		Method method = new Method("Criteria"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(true);
		method.addParameter(new Parameter(new FullyQualifiedJavaType("java.lang.String"),"tableName"));
		method.addBodyLine("super(tableName);"); //$NON-NLS-1$
		if (context.isJava8Targeted()) {
			method.addBodyLine("criteria = new ArrayList<>();"); //$NON-NLS-1$
		} else {
			method.addBodyLine("criteria = new ArrayList<Criterion>();"); //$NON-NLS-1$
		}
		answer.addMethod(method);


		method = new Method("Criteria");
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(true);
		method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "tableName"));
		method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "ignoreCase"));
		method.addBodyLine("this(tableName);");
		method.addBodyLine("this.ignoreCase = ignoreCase;");
		answer.addMethod(method);

		List<String> criteriaLists = new ArrayList<>();
		criteriaLists.add("criteria"); //$NON-NLS-1$

		for (IntrospectedColumn introspectedColumn : introspectedTable .getNonBLOBColumns()) {
			if (stringHasValue(introspectedColumn.getTypeHandler())) {
				String name = addtypeHandledObjectsAndMethods(introspectedColumn, method, answer);
				criteriaLists.add(name);
			}
		}

		// now generate the isValid method
		method = new Method("isValid"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType
				.getBooleanPrimitiveInstance());
		StringBuilder sb = new StringBuilder();
		Iterator<String> strIter = criteriaLists.iterator();
		sb.append("return "); //$NON-NLS-1$
		sb.append(strIter.next());
		sb.append(".size() > 0"); //$NON-NLS-1$
		if (!strIter.hasNext()) {
			sb.append(';');
		}
		method.addBodyLine(sb.toString());
		boolean hasHandle = false;
		while (strIter.hasNext()) {
			sb.setLength(0);
			OutputUtilities.javaIndent(sb, 1);
			sb.append("|| "); //$NON-NLS-1$
			sb.append(strIter.next());
			sb.append(".size() > 0"); //$NON-NLS-1$
			if (!strIter.hasNext()) {
				sb.append(';');
			}
			method.addBodyLine(sb.toString());
			hasHandle = true;
		}
		if(hasHandle) {
			answer.addMethod(method);
		}

		// now generate the getAllCriteria method
		if (criteriaLists.size() > 1) {
			field = new Field("allCriteria", //$NON-NLS-1$
					new FullyQualifiedJavaType("List<Criterion>")); //$NON-NLS-1$
			field.setVisibility(JavaVisibility.PROTECTED);
			answer.addField(field);
		}

		method = new Method("getAllCriteria"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(new FullyQualifiedJavaType("List<Criterion>")); //$NON-NLS-1$
		if (criteriaLists.size() < 2) {
			method.addBodyLine("return criteria;"); //$NON-NLS-1$
		} else {
			method.addBodyLine("if (allCriteria == null) {"); //$NON-NLS-1$
			if (context.isJava8Targeted()) {
				method.addBodyLine("allCriteria = new ArrayList<>();"); //$NON-NLS-1$
			} else {
				method.addBodyLine("allCriteria = new ArrayList<Criterion>();"); //$NON-NLS-1$
			}

			strIter = criteriaLists.iterator();
			while (strIter.hasNext()) {
				method.addBodyLine(String.format("allCriteria.addAll(%s);", strIter.next())); //$NON-NLS-1$
			}

			method.addBodyLine("}"); //$NON-NLS-1$
			method.addBodyLine("return allCriteria;"); //$NON-NLS-1$
		}
		answer.addMethod(method);

		// now we need to generate the methods that will be used in the SqlMap
		// to generate the dynamic where clause
		topLevelClass.addImportedType(FullyQualifiedJavaType
				.getNewListInstance());
		topLevelClass.addImportedType(FullyQualifiedJavaType
				.getNewArrayListInstance());

		/*FullyQualifiedJavaType listOfCriterion = new FullyQualifiedJavaType(
				"java.util.List<Criterion>"); //$NON-NLS-1$
		field = new Field("criteria", listOfCriterion); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PROTECTED);
		answer.addField(field);*/


		/*method = new Method(getGetterMethodName(field.getName(), field
				.getType()));
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(field.getType());
		method.addBodyLine("return criteria;"); //$NON-NLS-1$
		answer.addMethod(method);*/
		// now add the methods for simplifying the individual field set methods
		/*method = new Method("addCriterion"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addBodyLine("if (condition == null) {"); //$NON-NLS-1$
		method
				.addBodyLine("throw new RuntimeException(\"Value for condition cannot be null\");"); //$NON-NLS-1$
		method.addBodyLine("}"); //$NON-NLS-1$
		method.addBodyLine("criteria.add(new Criterion(condition));"); //$NON-NLS-1$
		if (criteriaLists.size() > 1) {
			method.addBodyLine("allCriteria = null;"); //$NON-NLS-1$
		}
		answer.addMethod(method);

		method = new Method("addCriterion"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "value")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "property")); //$NON-NLS-1$
		method.addBodyLine("if (value == null) {"); //$NON-NLS-1$
		method.addBodyLine(
				"throw new RuntimeException(\"Value for \" + property + \" cannot be null\");"); //$NON-NLS-1$
		method.addBodyLine("}"); //$NON-NLS-1$
		method.addBodyLine("criteria.add(new Criterion(condition, value));"); //$NON-NLS-1$
		if (criteriaLists.size() > 1) {
			method.addBodyLine("allCriteria = null;"); //$NON-NLS-1$
		}
		answer.addMethod(method);

		method = new Method("addCriterion"); //$NON-NLS-1$
		method.setVisibility(JavaVisibility.PROTECTED);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "value1")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "value2")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "property")); //$NON-NLS-1$
		method.addBodyLine("if (value1 == null || value2 == null) {"); //$NON-NLS-1$
		method.addBodyLine(
				"throw new RuntimeException(\"Between values for \" + property + \" cannot be null\");"); //$NON-NLS-1$
		method.addBodyLine("}"); //$NON-NLS-1$
		method
				.addBodyLine("criteria.add(new Criterion(condition, value1, value2));"); //$NON-NLS-1$
		if (criteriaLists.size() > 1) {
			method.addBodyLine("allCriteria = null;"); //$NON-NLS-1$
		}
		answer.addMethod(method);*/

		/*FullyQualifiedJavaType listOfDates = new FullyQualifiedJavaType(
				"java.util.List<java.util.Date>"); //$NON-NLS-1$

		if (introspectedTable.hasJDBCDateColumns()) {
			topLevelClass.addImportedType(FullyQualifiedJavaType
					.getDateInstance());
			topLevelClass.addImportedType(FullyQualifiedJavaType
					.getNewIteratorInstance());
			method = new Method("addCriterionForJDBCDate"); //$NON-NLS-1$
			method.setVisibility(JavaVisibility.PROTECTED);
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "condition")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getDateInstance(), "value")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "property")); //$NON-NLS-1$
			method.addBodyLine("if (value == null) {"); //$NON-NLS-1$
			method.addBodyLine(
					"throw new RuntimeException(\"Value for \" + property + \" cannot be null\");"); //$NON-NLS-1$
			method.addBodyLine("}"); //$NON-NLS-1$
			method.addBodyLine(
					"addCriterion(condition, new java.sql.Date(value.getTime()), property);"); //$NON-NLS-1$
			answer.addMethod(method);

			method = new Method("addCriterionForJDBCDate"); //$NON-NLS-1$
			method.setVisibility(JavaVisibility.PROTECTED);
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "condition")); //$NON-NLS-1$
			method.addParameter(new Parameter(listOfDates, "values")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "property")); //$NON-NLS-1$
			method.addBodyLine("if (values == null || values.size() == 0) {"); //$NON-NLS-1$
			method.addBodyLine(
					"throw new RuntimeException(\"Value list for \" + property + \"" //$NON-NLS-1$
							+ " cannot be null or empty\");"); //$NON-NLS-1$
			method.addBodyLine("}"); //$NON-NLS-1$
			if (context.isJava8Targeted()) {
				method.addBodyLine("List<java.sql.Date> dateList = new ArrayList<>();"); //$NON-NLS-1$
			} else {
				method.addBodyLine("List<java.sql.Date> dateList = new ArrayList<java.sql.Date>();"); //$NON-NLS-1$
			}
			method.addBodyLine("Iterator<Date> iter = values.iterator();"); //$NON-NLS-1$
			method.addBodyLine("while (iter.hasNext()) {"); //$NON-NLS-1$
			method
					.addBodyLine("dateList.add(new java.sql.Date(iter.next().getTime()));"); //$NON-NLS-1$
			method.addBodyLine("}"); //$NON-NLS-1$
			method.addBodyLine("addCriterion(condition, dateList, property);"); //$NON-NLS-1$
			answer.addMethod(method);

			method = new Method("addCriterionForJDBCDate"); //$NON-NLS-1$
			method.setVisibility(JavaVisibility.PROTECTED);
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "condition")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getDateInstance(), "value1")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getDateInstance(), "value2")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "property")); //$NON-NLS-1$
			method.addBodyLine("if (value1 == null || value2 == null) {"); //$NON-NLS-1$
			method.addBodyLine(
					"throw new RuntimeException(\"Between values for \" + property + \"" //$NON-NLS-1$
							+ " cannot be null\");"); //$NON-NLS-1$
			method.addBodyLine("}"); //$NON-NLS-1$
			method.addBodyLine(
					"addCriterion(condition, new java.sql.Date(value1.getTime())," //$NON-NLS-1$
							+ " new java.sql.Date(value2.getTime()), property);"); //$NON-NLS-1$
			answer.addMethod(method);
		}

		if (introspectedTable.hasJDBCTimeColumns()) {
			topLevelClass.addImportedType(FullyQualifiedJavaType
					.getDateInstance());
			topLevelClass.addImportedType(FullyQualifiedJavaType
					.getNewIteratorInstance());
			method = new Method("addCriterionForJDBCTime"); //$NON-NLS-1$
			method.setVisibility(JavaVisibility.PROTECTED);
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "condition")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getDateInstance(), "value")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "property")); //$NON-NLS-1$
			method.addBodyLine("if (value == null) {"); //$NON-NLS-1$
			method.addBodyLine(
					"throw new RuntimeException(\"Value for \" + property + \" cannot be null\");"); //$NON-NLS-1$
			method.addBodyLine("}"); //$NON-NLS-1$
			method.addBodyLine(
					"addCriterion(condition, new java.sql.Time(value.getTime()), property);"); //$NON-NLS-1$
			answer.addMethod(method);

			method = new Method("addCriterionForJDBCTime"); //$NON-NLS-1$
			method.setVisibility(JavaVisibility.PROTECTED);
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "condition")); //$NON-NLS-1$
			method.addParameter(new Parameter(listOfDates, "values")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "property")); //$NON-NLS-1$
			method.addBodyLine("if (values == null || values.size() == 0) {"); //$NON-NLS-1$
			method.addBodyLine(
					"throw new RuntimeException(\"Value list for \" + property + \"" //$NON-NLS-1$
							+ " cannot be null or empty\");"); //$NON-NLS-1$
			method.addBodyLine("}"); //$NON-NLS-1$
			if (context.isJava8Targeted()) {
				method.addBodyLine("List<java.sql.Time> timeList = new ArrayList<>();"); //$NON-NLS-1$
			} else {
				method.addBodyLine("List<java.sql.Time> timeList = new ArrayList<java.sql.Time>();"); //$NON-NLS-1$
			}
			method.addBodyLine("Iterator<Date> iter = values.iterator();"); //$NON-NLS-1$
			method.addBodyLine("while (iter.hasNext()) {"); //$NON-NLS-1$
			method
					.addBodyLine("timeList.add(new java.sql.Time(iter.next().getTime()));"); //$NON-NLS-1$
			method.addBodyLine("}"); //$NON-NLS-1$
			method.addBodyLine("addCriterion(condition, timeList, property);"); //$NON-NLS-1$
			answer.addMethod(method);

			method = new Method("addCriterionForJDBCTime"); //$NON-NLS-1$
			method.setVisibility(JavaVisibility.PROTECTED);
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "condition")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getDateInstance(), "value1")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getDateInstance(), "value2")); //$NON-NLS-1$
			method.addParameter(new Parameter(FullyQualifiedJavaType
					.getStringInstance(), "property")); //$NON-NLS-1$
			method.addBodyLine("if (value1 == null || value2 == null) {"); //$NON-NLS-1$
			method.addBodyLine(
					"throw new RuntimeException(\"Between values for \" + property + \"" //$NON-NLS-1$
							+ " cannot be null\");"); //$NON-NLS-1$
			method.addBodyLine("}"); //$NON-NLS-1$
			method.addBodyLine(
					"addCriterion(condition, new java.sql.Time(value1.getTime())," //$NON-NLS-1$
							+ " new java.sql.Time(value2.getTime()), property);"); //$NON-NLS-1$
			answer.addMethod(method);
		}
		*/ // comment by suman 在父类中实现
		for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
			topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());

			// here we need to add the individual methods for setting the
			// conditions for a field
			answer.addMethod(getSetNullMethod(introspectedColumn));
			answer.addMethod(getSetNotNullMethod(introspectedColumn));
			answer.addMethod(getSetEqualMethod(introspectedColumn));
			answer.addMethod(getSetNotEqualMethod(introspectedColumn));
			answer.addMethod(getSetGreaterThanMethod(introspectedColumn));
			answer.addMethod(getSetGreaterThenOrEqualMethod(introspectedColumn));
			answer.addMethod(getSetLessThanMethod(introspectedColumn));
			answer.addMethod(getSetLessThanOrEqualMethod(introspectedColumn));

			if (introspectedColumn.isJdbcCharacterColumn()) {
				answer.addMethod(getSetLikeMethod(introspectedColumn));
				answer.addMethod(getSetNotLikeMethod(introspectedColumn));
			}

			answer.addMethod(getSetInOrNotInMethod(introspectedColumn, true));
			answer.addMethod(getSetInOrNotInMethod(introspectedColumn, false));
			answer.addMethod(getSetBetweenOrNotBetweenMethod(introspectedColumn, true));
			answer.addMethod(getSetBetweenOrNotBetweenMethod(introspectedColumn, false));
		}

		return answer;
	}

	private Method getSetNullMethod(IntrospectedColumn introspectedColumn) {
		return getNoValueMethod(introspectedColumn, "IsNull", "is null"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetNotNullMethod(IntrospectedColumn introspectedColumn) {
		return getNoValueMethod(introspectedColumn, "IsNotNull", "is not null"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetEqualMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "EqualTo", "="); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetNotEqualMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "NotEqualTo", "<>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetGreaterThanMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "GreaterThan", ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetGreaterThenOrEqualMethod(
			IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn,
				"GreaterThanOrEqualTo", ">="); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetLessThanMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "LessThan", "<"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetLessThanOrEqualMethod(
			IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn,
				"LessThanOrEqualTo", "<="); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetLikeMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "Like", "like"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetNotLikeMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "NotLike", "not like"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSingleValueMethod(IntrospectedColumn introspectedColumn,
										String nameFragment, String operator) {

		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "and"); //$NON-NLS-1$
		sb.append(nameFragment);

		Method method = new Method(sb.toString());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(introspectedColumn
				.getFullyQualifiedJavaType(), "value")); //$NON-NLS-1$
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		sb.setLength(0);

		if (introspectedColumn.isJDBCDateColumn()) {
			sb.append("addCriterionForJDBCDate(\""); //$NON-NLS-1$
		} else if (introspectedColumn.isJDBCTimeColumn()) {
			sb.append("addCriterionForJDBCTime(\""); //$NON-NLS-1$
		} else if (stringHasValue(introspectedColumn
				.getTypeHandler())) {
			sb.append("add"); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
			sb.append("Criterion(\""); //$NON-NLS-1$
		} else {
			sb.append("addCriterion(\""); //$NON-NLS-1$
		}

		sb.append(MyBatis3FormattingUtilities
				.getAliasedActualColumnName(introspectedColumn));
		sb.append(' ');
		sb.append(operator);
		sb.append("\", "); //$NON-NLS-1$
		sb.append("value"); //$NON-NLS-1$
		sb.append(", \""); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty());
		sb.append("\");"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		method.addBodyLine("return this;"); //$NON-NLS-1$

		return method;
	}

	/**
	 * Generates methods that set between and not between conditions.
	 *
	 * @param introspectedColumn the introspected column
	 * @param betweenMethod true if between, else not between
	 * @return a generated method for the between or not between method
	 */
	private Method getSetBetweenOrNotBetweenMethod(
			IntrospectedColumn introspectedColumn, boolean betweenMethod) {

		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "and"); //$NON-NLS-1$
		if (betweenMethod) {
			sb.append("Between"); //$NON-NLS-1$
		} else {
			sb.append("NotBetween"); //$NON-NLS-1$
		}
		Method method = new Method(sb.toString());
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType type = introspectedColumn
				.getFullyQualifiedJavaType();

		method.addParameter(new Parameter(type, "value1")); //$NON-NLS-1$
		method.addParameter(new Parameter(type, "value2")); //$NON-NLS-1$
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

		sb.setLength(0);
		if (introspectedColumn.isJDBCDateColumn()) {
			sb.append("addCriterionForJDBCDate(\""); //$NON-NLS-1$
		} else if (introspectedColumn.isJDBCTimeColumn()) {
			sb.append("addCriterionForJDBCTime(\""); //$NON-NLS-1$
		} else if (stringHasValue(introspectedColumn
				.getTypeHandler())) {
			sb.append("add"); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
			sb.append("Criterion(\""); //$NON-NLS-1$
		} else {
			sb.append("addCriterion(\""); //$NON-NLS-1$
		}

		sb.append(MyBatis3FormattingUtilities
				.getAliasedActualColumnName(introspectedColumn));
		if (betweenMethod) {
			sb.append(" between"); //$NON-NLS-1$
		} else {
			sb.append(" not between"); //$NON-NLS-1$
		}
		sb.append("\", "); //$NON-NLS-1$
		sb.append("value1, value2"); //$NON-NLS-1$
		sb.append(", \""); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty());
		sb.append("\");"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		method.addBodyLine("return this;"); //$NON-NLS-1$

		return method;
	}

	/**
	 * Generates an In or NotIn method.
	 *
	 * @param introspectedColumn the introspected column
	 * @param inMethod
	 *            if true generates an "in" method, else generates a "not in"
	 *            method
	 * @return a generated method for the in or not in method
	 */
	private Method getSetInOrNotInMethod(IntrospectedColumn introspectedColumn,
										 boolean inMethod) {
		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "and"); //$NON-NLS-1$
		if (inMethod) {
			sb.append("In"); //$NON-NLS-1$
		} else {
			sb.append("NotIn"); //$NON-NLS-1$
		}
		Method method = new Method(sb.toString());
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType type = FullyQualifiedJavaType
				.getNewListInstance();
		if (introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
			type.addTypeArgument(introspectedColumn.getFullyQualifiedJavaType()
					.getPrimitiveTypeWrapper());
		} else {
			type
					.addTypeArgument(introspectedColumn
							.getFullyQualifiedJavaType());
		}

		method.addParameter(new Parameter(type, "values")); //$NON-NLS-1$
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

		sb.setLength(0);
		if (introspectedColumn.isJDBCDateColumn()) {
			sb.append("addCriterionForJDBCDate(\""); //$NON-NLS-1$
		} else if (introspectedColumn.isJDBCTimeColumn()) {
			sb.append("addCriterionForJDBCTime(\""); //$NON-NLS-1$
		} else if (stringHasValue(introspectedColumn
				.getTypeHandler())) {
			sb.append("add"); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
			sb.append("Criterion(\""); //$NON-NLS-1$
		} else {
			sb.append("addCriterion(\""); //$NON-NLS-1$
		}

		sb.append(MyBatis3FormattingUtilities
				.getAliasedActualColumnName(introspectedColumn));
		if (inMethod) {
			sb.append(" in"); //$NON-NLS-1$
		} else {
			sb.append(" not in"); //$NON-NLS-1$
		}
		sb.append("\", values, \""); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty());
		sb.append("\");"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		method.addBodyLine("return this;"); //$NON-NLS-1$

		return method;
	}

	private Method getNoValueMethod(IntrospectedColumn introspectedColumn,
									String nameFragment, String operator) {
		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "and"); //$NON-NLS-1$
		sb.append(nameFragment);
		Method method = new Method(sb.toString());

		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

		sb.setLength(0);
		sb.append("addCriterion(\""); //$NON-NLS-1$
		sb.append(MyBatis3FormattingUtilities
				.getAliasedActualColumnName(introspectedColumn));
		sb.append(' ');
		sb.append(operator);
		sb.append("\");"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		method.addBodyLine("return this;"); //$NON-NLS-1$

		return method;
	}

	/**
	 * This method adds all the extra methods and fields required to support a
	 * user defined type handler on some column.
	 *
	 * @param introspectedColumn the introspected column
	 * @param constructor the constructor
	 * @param innerClass the enclosing class
	 * @return the name of the List added to the class by this method
	 */
	private String addtypeHandledObjectsAndMethods(
			IntrospectedColumn introspectedColumn, Method constructor,
			InnerClass innerClass) {
		StringBuilder sb = new StringBuilder();

		// add new private field and public accessor in the class
		sb.setLength(0);
		sb.append(introspectedColumn.getJavaProperty());
		sb.append("Criteria"); //$NON-NLS-1$
		String answer = sb.toString();

		Field field = new Field(answer, new FullyQualifiedJavaType("java.util.List<Criterion>")); //$NON-NLS-1$
		field.setVisibility(JavaVisibility.PROTECTED);
		innerClass.addField(field);

		Method method = new Method(getGetterMethodName(field.getName(), field
				.getType()));
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(field.getType());
		sb.insert(0, "return "); //$NON-NLS-1$
		sb.append(';');
		method.addBodyLine(sb.toString());
		innerClass.addMethod(method);

		// add constructor initialization
		sb.setLength(0);
		sb.append(field.getName());
		if (context.isJava8Targeted()) {
			sb.append(" = new ArrayList<>();"); //$NON-NLS-1$
		} else {
			sb.append(" = new ArrayList<Criterion>();"); //$NON-NLS-1$
		}
		constructor.addBodyLine(sb.toString());

		// now add the methods for simplifying the individual field set methods
		sb.setLength(0);
		sb.append("add"); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
		sb.append("Criterion"); //$NON-NLS-1$
		method = new Method(sb.toString());
		method.setVisibility(JavaVisibility.PROTECTED);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getObjectInstance(), "value")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "property")); //$NON-NLS-1$
		method.addBodyLine("if (value == null) {"); //$NON-NLS-1$
		method.addBodyLine(
				"throw new RuntimeException(\"Value for \" + property + \" cannot be null\");"); //$NON-NLS-1$
		method.addBodyLine("}"); //$NON-NLS-1$

		method.addBodyLine(
				String.format("%s.add(new Criterion(condition, value, \"%s\"));", //$NON-NLS-1$
						field.getName(), introspectedColumn.getTypeHandler()));
		method.addBodyLine("allCriteria = null;"); //$NON-NLS-1$
		innerClass.addMethod(method);

		sb.setLength(0);
		sb.append("add"); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
		sb.append("Criterion"); //$NON-NLS-1$

		method = new Method(sb.toString());
		method.setVisibility(JavaVisibility.PROTECTED);
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "condition")); //$NON-NLS-1$
		method.addParameter(new Parameter(introspectedColumn
				.getFullyQualifiedJavaType(), "value1")); //$NON-NLS-1$
		method.addParameter(new Parameter(introspectedColumn
				.getFullyQualifiedJavaType(), "value2")); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType
				.getStringInstance(), "property")); //$NON-NLS-1$
		if (!introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
			method.addBodyLine("if (value1 == null || value2 == null) {"); //$NON-NLS-1$
			method.addBodyLine(
					"throw new RuntimeException(\"Between values for \" + property + \"" //$NON-NLS-1$
							+ " cannot be null\");"); //$NON-NLS-1$
			method.addBodyLine("}"); //$NON-NLS-1$
		}

		method.addBodyLine(
				String.format("%s.add(new Criterion(condition, value1, value2, \"%s\"));", //$NON-NLS-1$
						field.getName(), introspectedColumn.getTypeHandler()));

		method.addBodyLine("allCriteria = null;"); //$NON-NLS-1$
		innerClass.addMethod(method);

		return answer;
	}

	private Method getCreateOtherExampleColumnsMethod(IntrospectedTable introspectedImportTable){
		FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getExampleType());
		StringBuffer sb = new StringBuffer();
		sb.append("create");
		sb.append(importType.getShortName().replace("Example", ""));
		sb.append("Columns");
		Method method = new Method(sb.toString());
		method.setVisibility(JavaVisibility.PUBLIC);

		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(".ColumnContainer");
		method.setReturnType(new FullyQualifiedJavaType(sb.toString()));
		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(" ");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(" = new ");
		sb.append(importType.getShortName());
		sb.append("();");
		method.addBodyLine(sb.toString());
		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(".ColumnContainer columnContainer = (");
		sb.append(importType.getShortName());
		sb.append(".ColumnContainer) columnContainerMap.get(");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(".getTableName());");
		method.addBodyLine(sb.toString());
		method.addBodyLine(" if(columnContainer == null){");
		sb.setLength(0);
		sb.append("columnContainer = ");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(".createColumns();");
		method.addBodyLine(sb.toString());
		sb.setLength(0);
		sb.append("columnContainerMap.put(");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(".getTableName(),columnContainer);");
		method.addBodyLine(sb.toString());
		sb.setLength(0);
		method.addBodyLine("}");
		method.addBodyLine("leftJoinTableSet.add(columnContainer.getTableName());");
		method.addBodyLine("return columnContainer;");
		return method;
	}
	private Method getAndOtherExampleCriteriaMethod(IntrospectedTable introspectedImportTable){
		FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getExampleType());
		StringBuffer sb = new StringBuffer();
		sb.append("and");
		sb.append(importType.getShortName().replace("Example", ""));
		sb.append("Criteria");
		Method method = new Method(sb.toString());
		method.setVisibility(JavaVisibility.PUBLIC);
		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(".Criteria");
		method.setReturnType(new FullyQualifiedJavaType(sb.toString()));
		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(" ");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(" = new ");
		sb.append(importType.getShortName());
		sb.append("();");
		method.addBodyLine(sb.toString());
		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(".Criteria criteria = ");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(".createCriteria();");
		method.addBodyLine(sb.toString());
		method.addBodyLine("Criteria myCriteria = null;");
		method.addBodyLine("if (oredCriteria.size() == 0) {");
		method.addBodyLine("myCriteria =  createCriteriaInternal();");
		method.addBodyLine("oredCriteria.add(myCriteria);");
		method.addBodyLine("}else{");
		method.addBodyLine("myCriteria =  (Criteria)oredCriteria.get(0);");
		method.addBodyLine("}");
		method.addBodyLine("leftJoinTableSet.add(criteria.getTableName());");
		method.addBodyLine("criteria.setAllCriteria(myCriteria.getAllCriteria());");
		method.addBodyLine("return criteria;");
		return method;
	}
	private Method getAndOtherExampleCriteriaMethodWithCriteria(IntrospectedTable introspectedImportTable){
		FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getExampleType());
		StringBuffer sb = new StringBuffer();
		sb.append("and");
		sb.append(importType.getShortName().replace("Example", ""));
		sb.append("Criteria");

		Method method = new Method(sb.toString());
		method.addParameter(new Parameter(new FullyQualifiedJavaType("Criteria"), "criteria"));
		method.setVisibility(JavaVisibility.PUBLIC);

		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(".Criteria");
		method.setReturnType(new FullyQualifiedJavaType(sb.toString()));
		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(" ");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(" = new ");
		sb.append(importType.getShortName());
		sb.append("();");
		method.addBodyLine(sb.toString());
		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(".Criteria newCriteria = ");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(".createCriteria();");
		method.addBodyLine(sb.toString());
		method.addBodyLine("leftJoinTableSet.add(newCriteria.getTableName());");
		method.addBodyLine("newCriteria.setAllCriteria(criteria.getAllCriteria());");
		method.addBodyLine("return newCriteria;");
		return method;
	}

	private Method getOrOtherExampleCriteriaMethod(IntrospectedTable introspectedImportTable){
		FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getExampleType());
		StringBuffer sb = new StringBuffer();
		sb.append("or");
		sb.append(importType.getShortName().replace("Example", ""));
		sb.append("Criteria");
		Method method = new Method(sb.toString());
		method.setVisibility(JavaVisibility.PUBLIC);

		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(".Criteria");
		method.setReturnType(new FullyQualifiedJavaType(sb.toString()));
		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(" ");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(" = new ");
		sb.append(importType.getShortName());
		sb.append("();");
		method.addBodyLine(sb.toString());
		sb.setLength(0);
		sb.append(importType.getShortName());
		sb.append(".Criteria criteria = ");
		sb.append(getValidPropertyName(importType.getShortName()));
		sb.append(".createCriteria();");
		method.addBodyLine(sb.toString());
		method.addBodyLine("leftJoinTableSet.add(criteria.getTableName());");
		method.addBodyLine("oredCriteria.add(criteria);");
		method.addBodyLine("return criteria;");
		return method;
	}

	private InnerClass getColumnContainerClass(TopLevelClass topLevelClass) {
		Field field;
		Method method;

		InnerClass answer = new InnerClass(FullyQualifiedJavaType.getColumnContainerInstance());

		answer.setVisibility(JavaVisibility.PUBLIC);

		answer.setStatic(true);
		answer.setSuperClass(FullyQualifiedJavaType.getColumnContainerBaseInstance());
		context.getCommentGenerator().addClassComment(answer, introspectedTable);

		method = new Method("ColumnContainer");
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(true);
		method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "tableName"));
		method.addBodyLine("super(tableName);");

		answer.addMethod(method);

		for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
			topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
			answer.addMethod(getHasColumnMethod(introspectedColumn));
		}

		return answer;
	}

	private Method getHasColumnMethod(IntrospectedColumn introspectedColumn) {
		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "has");
		sb.append("Column");
		Method method = new Method(sb.toString());
		method.setVisibility(JavaVisibility.PUBLIC);

		method.setReturnType(FullyQualifiedJavaType.getColumnContainerInstance());
		sb.setLength(0);
		sb.append("addColumnStr(\"");
		sb.append(MyBatis3FormattingUtilities.getSelectListPhrase(introspectedColumn).replace("\"", "\\\""));
		sb.append(' ');
		sb.append("\");");
		method.addBodyLine(sb.toString());
		method.addBodyLine("return (ColumnContainer) this;");

		return method;
	}

	private void buildWrapMethod(TopLevelClass topLevelClass){
		Method method = new Method("wrap");
		method.addAnnotation("@Override");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(new FullyQualifiedJavaType("List"),"values"));
		List<IntrospectedColumn> foreignKeyColumns = introspectedTable.getForeignKeyColumns();
		if(foreignKeyColumns ==null || foreignKeyColumns.isEmpty()){
			return ;
		}
		topLevelClass.addImportedType("java.util.Map");
		topLevelClass.addImportedType("java.util.stream.Collectors");
		for (IntrospectedColumn foreignKeyColumn : foreignKeyColumns) {

			IntrospectedColumn introspectedImportColumn = foreignKeyColumn.getIntrospectedImportColumn();
			IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
			FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType());
			String property = getValidPropertyName(importType.getShortName());
			method.addBodyLine(String.format("if(%s !=null && !%s.isEmpty()){",property+"s",property+"s" ));
			String mapName = property+"Map";
			method.addBodyLine(String.format("Map<%s,%s> %s = %s.stream().collect(Collectors.toMap(%s::%s, item -> item));",
					foreignKeyColumn.getFullyQualifiedJavaType().getShortName(),importType.getShortName(),mapName,
					property+"s",importType.getShortName(),getGetterMethodName(introspectedImportColumn.getJavaProperty(),introspectedImportColumn.getFullyQualifiedJavaType())));
			method.addBodyLine(" for (Object value : values) {");
			String modelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()).getShortName();
			method.addBodyLine(String.format("%s %s =  ((%s)value);",modelType,getValidPropertyName(modelType),modelType));
			method.addBodyLine(String.format("%s.%s(%s.get(%s.%s()));",getValidPropertyName(modelType),getSetterMethodName(property),mapName,getValidPropertyName(modelType),getGetterMethodName(foreignKeyColumn.getJavaProperty(),foreignKeyColumn.getFullyQualifiedJavaType())));
			method.addBodyLine("}");
			method.addBodyLine("}");
		}

		topLevelClass.addMethod(method);
	}

	private void buildSetComplexPropertyMethod(TopLevelClass topLevelClass){
		Method method = new Method("setComplexProperty");
		method.addAnnotation("@Override");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(),"property"));
		method.addParameter(new Parameter(new FullyQualifiedJavaType("List"),"values"));
		StringBuilder sb = new StringBuilder();
		List<IntrospectedColumn> foreignKeyColumns = introspectedTable.getForeignKeyColumns();
		for (IntrospectedColumn foreignKeyColumn : foreignKeyColumns) {

			IntrospectedColumn introspectedImportColumn = foreignKeyColumn.getIntrospectedImportColumn();
			IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
			FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType());
			String property = getValidPropertyName(importType.getShortName());
			Field field = new Field(property+"s",new FullyQualifiedJavaType("List<"+importType.getShortName()+">"));
			field.setVisibility(JavaVisibility.PRIVATE);
			field.setInitializationString("new ArrayList<>();");
			topLevelClass.addField(field);
			sb.setLength(0);
			sb.append("if(FIELD_");
			sb.append(new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType()).getShortName().toUpperCase());
			sb.append(".equals(property)) {");
			method.addBodyLine(sb.toString());
			method.addBodyLine(property+"s.addAll(values);");
			method.addBodyLine("}");

		}

		topLevelClass.addMethod(method);
	}

}
