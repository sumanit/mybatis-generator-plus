/*
 *  Copyright 2009 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.model;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class BaseRecordDTOBaseGenerator extends AbstractJavaGenerator {

	public static final String DTO_BASE_INTERFACE = "com.github.sumanit.base.DTOInterface";

	public BaseRecordDTOBaseGenerator(String project) {
		super(project);
	}
	
	
	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.8", table.toString()));
		Plugin plugins = context.getPlugins();
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordDTOBaseType());
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);

		FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		if (superClass != null) {
			topLevelClass.setSuperClass(superClass);
			topLevelClass.addImportedType(superClass);
		}
		FullyQualifiedJavaType rootInterface = new FullyQualifiedJavaType(DTO_BASE_INTERFACE +"<"+superClass.getShortName()+">");
		if (rootInterface != null) {
			topLevelClass.addSuperInterface(rootInterface);
			topLevelClass.addImportedType(rootInterface);
		}
		addDTOBaseConstructor(topLevelClass);

		String s = String.format("@ApiModel(value=\"%s\")",new FullyQualifiedJavaType(introspectedTable.getBaseRecordDTOType()).getShortName()+"-"+introspectedTable.getRemarks());
		topLevelClass.addAnnotation(s);
		topLevelClass.addImportedType("io.swagger.annotations.ApiModel");

		Method getModelMethod = new Method("getModel");
		getModelMethod.addAnnotation("@JsonIgnore");
		getModelMethod.setVisibility(JavaVisibility.PUBLIC);
		getModelMethod.setReturnType(superClass);
		StringBuilder sb = new StringBuilder();
		sb.append("return ").append(getValidPropertyName(superClass.getShortName()));
		sb.append(";");
		getModelMethod.addBodyLine(sb.toString());
		topLevelClass.addMethod(getModelMethod);
		
		
		Method setModelMethod = new Method("setModel");
		setModelMethod.setVisibility(JavaVisibility.PUBLIC);
		setModelMethod.addParameter(new Parameter(superClass,getValidPropertyName(superClass.getShortName())));
		sb.setLength(0);
		sb.append("this.").append(getValidPropertyName(superClass.getShortName())).append(" = ");
		sb.append(getValidPropertyName(superClass.getShortName()));
		sb.append(";");
		setModelMethod.addBodyLine(sb.toString());
		
		topLevelClass.addMethod(setModelMethod);
		
		List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();

	
		if (introspectedTable.isConstructorBased()) {
			addParameterizedConstructor(topLevelClass);

			if (!introspectedTable.isImmutable()) {
				addDefaultConstructor(topLevelClass);
			}
			
		}

		String rootClass = getRootClass();
		topLevelClass.addImportedType( new FullyQualifiedJavaType("com.fasterxml.jackson.annotation.JsonIgnore"));
		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
				continue;
			}
			FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
			Method method = getJavaBeansGetter(introspectedColumn, context, introspectedTable);
			if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
				method.getBodyLines().clear();

				method.addBodyLine("if(getModel() == null ){");
				method.addBodyLine("throw new RuntimeException(\"model is null\");");
				method.addBodyLine("}");

				sb.setLength(0);
				sb.append("return "); //$NON-NLS-1$
				sb.append("this.");
				sb.append("getModel()").append(".");
				sb.append(method.getName()).append("(");
				sb.append(")");
				sb.append(';');
				method.addBodyLine(sb.toString());

				topLevelClass.addMethod(method);
			}

			if (!introspectedTable.isImmutable()) {
				method = getJavaBeansSetter(introspectedColumn, context, introspectedTable);
				if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {

					method.getBodyLines().clear();
					method.addBodyLine("if(getModel() == null ){");
					method.addBodyLine("throw new RuntimeException(\"model is null\");");
					method.addBodyLine("}");
					sb.setLength(0);
					sb.append("this.");
					sb.append("getModel()").append(".");
					sb.append(method.getName()).append("(");
					sb.append(introspectedColumn.getJavaProperty());
					sb.append(")");
					sb.append(';');
					method.addBodyLine(sb.toString());
					topLevelClass.addMethod(method);
				}
			}
		}
		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
				continue;
			}

			FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
			String property = introspectedColumn.getJavaProperty();
			FullyQualifiedJavaType arrType = new FullyQualifiedJavaType("java.util.List<"+fqjt.getShortName()+">");
			topLevelClass.addImportedType("java.util.List");
			createFieldAndMethod(arrType,property,"_arr",topLevelClass,introspectedColumn);
			if(introspectedColumn.isNullable()) {

				FullyQualifiedJavaType t = new FullyQualifiedJavaType("java.lang.Boolean");
				createFieldAndMethod(t,property,"_null",topLevelClass,introspectedColumn);
			}


			if (introspectedColumn.isJdbcCharacterColumn()) {//字符串
				createFieldAndMethod(fqjt,property,"_like",topLevelClass,introspectedColumn);
			}else{

				createFieldAndMethod(fqjt,property,"_lt",topLevelClass,introspectedColumn);

				createFieldAndMethod(fqjt,property,"_lte",topLevelClass,introspectedColumn);
				createFieldAndMethod(fqjt,property,"_gt",topLevelClass,introspectedColumn);
				createFieldAndMethod(fqjt,property,"_gte",topLevelClass,introspectedColumn);
				
			}
		}
		
		// add by suman start
		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
				continue;
			}
			IntrospectedColumn introspectedImportColumn = introspectedColumn.getIntrospectedImportColumn();
			if(introspectedImportColumn == null ){
				continue;
			}
			topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
			IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();

			FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordDTOType());
			topLevelClass.addImportedType(importType);
			FullyQualifiedJavaType importBaseModelType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType());
			Field field = new Field(getValidPropertyName(importBaseModelType.getShortName()),importType);
			field.setVisibility(JavaVisibility.PRIVATE);
			topLevelClass.addField(field);
			Method method = getImportJavaBeansGetter(introspectedColumn, context);
			if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
				topLevelClass.addMethod(method);
			}

			if (!introspectedTable.isImmutable()) {
				method = getImportJavaBeansSetter(introspectedColumn, context);
				if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
					topLevelClass.addMethod(method);
				}
			}
		}
				// add by suman end
		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}

	private void createFieldAndMethod(FullyQualifiedJavaType fieldType, String property,String suffix,TopLevelClass topLevelClass,IntrospectedColumn introspectedColumn){
		Field field = new Field(property+suffix,fieldType);
		field.setVisibility(JavaVisibility.PRIVATE);
		field.addAnnotation("@JsonIgnore");
		String annotation = "@ApiParam(value = \"%s(%s) %s 该门限\")";
		if("_lt".equals(suffix)){
			annotation = String.format(annotation, introspectedColumn.getRemarks()==null?property:introspectedColumn.getRemarks(),property,"小于");
		}else if("_lte".equals(suffix)){
			annotation = String.format(annotation, introspectedColumn.getRemarks()==null?property:introspectedColumn.getRemarks(),property,"小于等于");
		}else if("_gt".equals(suffix)){
			annotation = String.format(annotation, introspectedColumn.getRemarks()==null?property:introspectedColumn.getRemarks(),property,"大于");
		}else if("_gte".equals(suffix)){
			annotation = String.format(annotation, introspectedColumn.getRemarks()==null?property:introspectedColumn.getRemarks(),property,"大于等于");
		}else if("_like".equals(suffix)){
			annotation = String.format("@ApiParam(value = \"%s(%s) like 该值(需自己在前后加%%)\")", introspectedColumn.getRemarks()==null?property:introspectedColumn.getRemarks(),property);
		}else if("_arr".equals(suffix)){
			annotation = String.format("@ApiParam(value = \"%s(%s) 在该数组中\")", introspectedColumn.getRemarks()==null?property:introspectedColumn.getRemarks(),property);
		}else if("_null".equals(suffix)){
			annotation = "@ApiParam(value = \"该属性不为null\")";
		}
		topLevelClass.addImportedType("io.swagger.annotations.ApiParam");
		field.addAnnotation(annotation);
		topLevelClass.addField(field);
		topLevelClass.addImportedType(field.getType());

		Method method = new Method(getGetterMethodName(field.getName(),fieldType));
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(fieldType);
		method.addBodyLine("return "+property+suffix+";");
		topLevelClass.addMethod(method);

		method = new Method(getSetterMethodName(field.getName()));
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(fieldType,property+suffix));
		method.addBodyLine("this."+property+suffix+" = "+property+suffix+";" );
		topLevelClass.addMethod(method);
	}

	private void addDTOBaseConstructor(TopLevelClass topLevelClass) {
		
		FullyQualifiedJavaType baseModelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		Field field = new Field(getValidPropertyName(baseModelType.getShortName()),baseModelType);
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setType(baseModelType);
		topLevelClass.addField(field);
		topLevelClass.addImportedType(field.getType());
		
		Method method = new Method(topLevelClass.getType().getShortName());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		method.addBodyLine("this(null);");
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
		method = new Method(topLevelClass.getType().getShortName());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		
		method.addParameter(new Parameter(baseModelType,getValidPropertyName(baseModelType.getShortName()) ));
		
		StringBuilder sb = new StringBuilder();
		sb.append("if(");
		sb.append(getValidPropertyName(baseModelType.getShortName())).append(" == null");
		sb.append(") {");
		method.addBodyLine(sb.toString());
		sb.setLength(0);
		sb.append(getValidPropertyName(baseModelType.getShortName())).append(" = new ").append(baseModelType.getShortName()).append("();");
		method.addBodyLine(sb.toString());
		method.addBodyLine("}");
		
		sb.setLength(0);
		sb.append("this.").append(field.getName()).append(" = ").append(getValidPropertyName(baseModelType.getShortName())).append(";");
		method.addBodyLine(sb.toString());
		topLevelClass.addMethod(method);
	}


	private FullyQualifiedJavaType getSuperClass() {
		FullyQualifiedJavaType superClass;
		if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			superClass = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
		} else {
			String rootClass = getRootClass();
			if (rootClass != null) {
				superClass = new FullyQualifiedJavaType(rootClass);
			} else {
				superClass = null;
			}
		}

		return superClass;
	}

	private boolean includePrimaryKeyColumns() {
		return !introspectedTable.getRules().generatePrimaryKeyClass() && introspectedTable.hasPrimaryKeyColumns();
	}

	protected boolean includeBLOBColumns() {
		return !introspectedTable.getRules().generateRecordWithBLOBsClass() && introspectedTable.hasBLOBColumns();
	}

	protected void addParameterizedConstructor(TopLevelClass topLevelClass) {
		Method method = new Method(topLevelClass.getType().getShortName());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

		List<IntrospectedColumn> constructorColumns = includeBLOBColumns() ? introspectedTable.getAllColumns() : introspectedTable.getNonBLOBColumns();

		for (IntrospectedColumn introspectedColumn : constructorColumns) {
			method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty()));
			topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
		}

		StringBuilder sb = new StringBuilder();
		if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			boolean comma = false;
			sb.append("super(");
			for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
				if (comma) {
					sb.append(", ");
				} else {
					comma = true;
				}
				sb.append(introspectedColumn.getJavaProperty());
			}
			sb.append(");");
			method.addBodyLine(sb.toString());
		}

		List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();

		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			sb.setLength(0);
			sb.append("this.");
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(" = ");
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(';');
			method.addBodyLine(sb.toString());
		}

		topLevelClass.addMethod(method);
	}

	protected List<IntrospectedColumn> getColumnsInThisClass() {
		List<IntrospectedColumn> introspectedColumns;
		if (includePrimaryKeyColumns()) {
			if (includeBLOBColumns()) {
				introspectedColumns = introspectedTable.getAllColumns();
			} else {
				introspectedColumns = introspectedTable.getNonBLOBColumns();
			}
		} else {
			if (includeBLOBColumns()) {
				introspectedColumns = introspectedTable.getNonPrimaryKeyColumns();
			} else {
				introspectedColumns = introspectedTable.getBaseColumns();
			}
		}

		return introspectedColumns;
	}
	// add by suman start
	protected Method getImportJavaBeansSetter(IntrospectedColumn introspectedColumn, Context context) {
		IntrospectedColumn introspectedImportColumn = introspectedColumn.getIntrospectedImportColumn();
		if(introspectedImportColumn == null ){
			return null;
		}

		IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
		FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType());
		String property = getValidPropertyName(importType.getShortName());

		Method method = new Method(getSetterMethodName(property));
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordDTOType()), property));
		context.getCommentGenerator().addSetterComment(method, introspectedImportTable, introspectedColumn);

		StringBuilder sb = new StringBuilder();

		sb.append("this."); //$NON-NLS-1$
		sb.append(property);
		sb.append(" = "); //$NON-NLS-1$
		sb.append(property);
		sb.append(';');
		method.addBodyLine(sb.toString());


		return method;
	}

	protected Method getImportJavaBeansGetter(IntrospectedColumn introspectedColumn, Context context) {
		IntrospectedColumn introspectedImportColumn = introspectedColumn.getIntrospectedImportColumn();
		if(introspectedImportColumn == null ){
			return null;
		}

		IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
		FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordDTOType());
		String property = getValidPropertyName(new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType()).getShortName());

		Method method = new Method(getGetterMethodName(property, importType));
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(importType);
		context.getCommentGenerator().addGetterComment(method, introspectedImportTable, introspectedColumn);
		method.addAnnotation("@ApiModelProperty(hidden=true)");
		method.addAnnotation("@ApiParam(hidden=true)");
		StringBuilder sb = new StringBuilder();
		sb.append("return "); //$NON-NLS-1$
		sb.append(property);
		sb.append(';');
		method.addBodyLine(sb.toString());

		return method;
	}

	// add by suman end
}
