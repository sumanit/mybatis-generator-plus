/*
 *  Copyright 2012 The MyBatis Team
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
package org.mybatis.generator.codegen.mybatis3.controller;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getValidPropertyName;
import static org.mybatis.generator.internal.util.messages.Messages.getString;
import static org.mybatis.generator.internal.util.JavaBeansUtil.*;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class ControllerBaseGenerator extends AbstractJavaGenerator {

	public ControllerBaseGenerator(String project) {
		super(project);
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
		Plugin plugins = context.getPlugins();
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType().replace(".model", ".controller.base")+"BaseController");
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		topLevelClass.setAbstract(true);
		commentGenerator.addJavaFileComment(topLevelClass);

		FullyQualifiedJavaType superClass = getSuperClass();

		if (superClass != null) {
			topLevelClass.setSuperClass(superClass);
			topLevelClass.addImportedType(superClass);
		}

		topLevelClass.addImportedType( new FullyQualifiedJavaType("javax.annotation.Resource"));
		topLevelClass.addImportedType( new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

		if(introspectedTable.getRemarks()==null||introspectedTable.getRemarks().isEmpty()){
			topLevelClass.addAnnotation("@Api(tags=\""+type.getShortName()+"\")");
		}else{
			topLevelClass.addAnnotation("@Api(tags=\""+introspectedTable.getRemarks()+"\")");
		}

		topLevelClass.addImportedType("io.swagger.annotations.Api");


		FullyQualifiedJavaType serviceType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType().replaceAll("Mapper", "Service").replaceAll(".mapper", ".service.adapter"));
		topLevelClass.addImportedType(serviceType);

		Field field = new Field(getValidPropertyName(serviceType.getShortName()),serviceType);
		field.addAnnotation("@Resource");
		field.setVisibility(JavaVisibility.PROTECTED);
		topLevelClass.addField(field);


		createGetExampleMethod(topLevelClass);
		createSetPrimaryKeyMethod(topLevelClass);
		
		FullyQualifiedJavaType baseServiceType = new FullyQualifiedJavaType(
				introspectedTable.getMyBatis3JavaMapperType().replaceAll("Mapper", "Service").replaceAll(".mapper", ".service.adapter"));
		topLevelClass.addImportedType(baseServiceType);
		Method method = new Method("getService");
		method.addAnnotation("@Override");
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(false);
		method.setReturnType(baseServiceType);
		method.addBodyLine("return "+getValidPropertyName(serviceType.getShortName())+";");
		topLevelClass.addMethod(method);
		
		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}



	private void createSetPrimaryKeyMethod(TopLevelClass topLevelClass) {

		Method method = new Method("setPrimaryKey");
		method.addAnnotation("@Override");
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(false);
		FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordDTOType());
		Parameter parameter = new Parameter(modelType,getValidPropertyName(modelType.getShortName()));
		method.addParameter(parameter);
		parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(),"primaryKey");
		method.addParameter(parameter);
		if(introspectedTable.getPrimaryKeyColumns() != null && introspectedTable.getPrimaryKeyColumns().size() >0) {
			IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
			String property = introspectedColumn.getJavaProperty();
			StringBuilder sb = new StringBuilder();
			sb.append(getValidPropertyName(modelType.getShortName())).append(".");
			sb.append(getSetterMethodName(property)).append("(");
			if("String".equals(introspectedColumn.getFullyQualifiedJavaType().getShortName())) {
				sb.append("primaryKey");
			} else if("Long".equals(introspectedColumn.getFullyQualifiedJavaType().getShortName())) {
				sb.append("Long.parseLong(primaryKey)");
			} else if("Integer".equals(introspectedColumn.getFullyQualifiedJavaType().getShortName())) {
				sb.append("Integer.parseInt(primaryKey)");
			}
			sb.append(");");
			method.addBodyLine(sb.toString());
		}
		topLevelClass.addMethod(method);
	}

	private FullyQualifiedJavaType getSuperClass() {
		FullyQualifiedJavaType superClass;
		String rootClass = "com.github.sumanit.base.BaseController<"+introspectedTable.getBaseRecordType()+","+introspectedTable.getBaseRecordDTOType()+","+introspectedTable.getBaseRecordType()+"Example>";
		if (rootClass != null) {
			superClass = new FullyQualifiedJavaType(rootClass);
		} else {
			superClass = null;
		}

		return superClass;
	}
	private void createGetExampleMethod(TopLevelClass topLevelClass){
		Method method = new Method("getExample");
		method.addAnnotation("@Override");
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setConstructor(false);
		FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordDTOType());
		FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
		Parameter parameter = new Parameter(modelType,getValidPropertyName(modelType.getShortName()));
		method.addParameter(parameter);
		FullyQualifiedJavaType queryModelType = new FullyQualifiedJavaType("com.github.sumanit.base.QueryModel");
		topLevelClass.addImportedType(queryModelType);
		parameter = new Parameter(queryModelType,"queryModel");
		method.addParameter(parameter);
		topLevelClass.addImportedType(exampleType);
		method.setReturnType(exampleType);
		StringBuilder sb = new StringBuilder();
		sb.append(exampleType.getShortName()).append(" ").append(getValidPropertyName(exampleType.getShortName())).append(" = ").append("super.getExample(").append(getValidPropertyName(modelType.getShortName())).append(",queryModel);");
		method.addBodyLine(sb.toString());
		method.addBodyLine("return "+getValidPropertyName(exampleType.getShortName())+";");
		topLevelClass.addMethod(method);
	}


}
