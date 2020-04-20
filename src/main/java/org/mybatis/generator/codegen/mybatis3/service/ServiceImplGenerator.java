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
package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getValidPropertyName;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class ServiceImplGenerator extends AbstractJavaGenerator {

	public ServiceImplGenerator(String project) {
		super(project);
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
		Plugin plugins = context.getPlugins();
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType().replace(".model", ".service.impl")+"ServiceImpl");
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);

		FullyQualifiedJavaType superClass = getSuperClass();
		if (superClass != null) {
			topLevelClass.setSuperClass(superClass);
			topLevelClass.addImportedType(superClass);
		}
		topLevelClass.addAnnotation("@Service");
		topLevelClass.addImportedType( new FullyQualifiedJavaType("org.springframework.stereotype.Service"));
		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}

	private FullyQualifiedJavaType getSuperClass() {
		FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType().replaceAll("Mapper", "Service").replaceAll(".mapper", ".service.base")+"Base");
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
		FullyQualifiedJavaType baseMapperType = new FullyQualifiedJavaType("com.github.sumanit.base.Mapper<"+introspectedTable.getBaseRecordType()+","+introspectedTable.getBaseRecordType()+"Example>");
		topLevelClass.addImportedType(baseMapperType);
		topLevelClass.addImportedType(exampleType);
		method.setReturnType(exampleType);
		StringBuilder sb = new StringBuilder();
		sb.append(exampleType.getShortName()).append(" ").append(getValidPropertyName(exampleType.getShortName())).append(" = new ")
				.append(exampleType.getShortName()).append("();");
		method.addBodyLine(sb.toString());
		sb.setLength(0);
		sb.append(exampleType.getShortName()).append(".Criteria criteria = ").append(getValidPropertyName(exampleType.getShortName()))
				.append(".createCriteria();");
		method.addBodyLine(sb.toString());
		List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();

		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
			String property = introspectedColumn.getJavaProperty();
			sb.setLength(0);
			sb.append(property);
			if (Character.isLowerCase(sb.charAt(0))) {
				if (sb.length() == 1 || !Character.isUpperCase(sb.charAt(1))) {
					sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
				}
			}
			property = sb.toString();
			String getMethod = getGetterMethodName(property,fqjt);
			sb.setLength(0);
			sb.append("if(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("() != null) {");
			method.addBodyLine(sb.toString());
			sb.setLength(0);
			sb.append("criteria.and").append(property).append("EqualTo(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("());");
			method.addBodyLine(sb.toString());
			sb.setLength(0);
			sb.append("}");
			method.addBodyLine(sb.toString());

			if(introspectedColumn.isNullable()){
				getMethod = getGetterMethodName(property+"_null",fqjt);
				sb.setLength(0);
				sb.append("if(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("() != null) {");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("if(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("().booleanValue()) {");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("criteria.and").append(property).append("IsNull();");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("} else {");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("criteria.and").append(property).append("IsNotNull();");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("}");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("}");
				method.addBodyLine(sb.toString());

			}

			//if(introspectedColumn.getIntrospectedImportColumn() != null || introspectedColumn.isIdentity()){\
			//所有的字段都添加in的sql查询
			getMethod = getGetterMethodName(property+"_arr",fqjt);
			sb.setLength(0);
			sb.append("if(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("() != null) {");
			method.addBodyLine(sb.toString());
			sb.setLength(0);
			sb.append("criteria.and").append(property).append("In(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("());");
			method.addBodyLine(sb.toString());
			sb.setLength(0);
			sb.append("}");
			method.addBodyLine(sb.toString());


			if (introspectedColumn.isJdbcCharacterColumn()) {//字符串

				getMethod = getGetterMethodName(property+"_like",fqjt);
				sb.setLength(0);
				sb.append("if(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("() != null) {");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("criteria.and").append(property).append("Like(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("());");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("}");
				method.addBodyLine(sb.toString());

			}else{
				getMethod = getGetterMethodName(property+"_gt",fqjt);
				sb.setLength(0);
				sb.append("if(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("() != null) {");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("criteria.and").append(property).append("GreaterThan(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("());");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("}");
				method.addBodyLine(sb.toString());

				getMethod = getGetterMethodName(property+"_lt",fqjt);
				sb.setLength(0);
				sb.append("if(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("() != null) {");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("criteria.and").append(property).append("LessThan(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("());");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("}");
				method.addBodyLine(sb.toString());


				getMethod = getGetterMethodName(property+"_gte",fqjt);
				sb.setLength(0);
				sb.append("if(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("() != null) {");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("criteria.and").append(property).append("GreaterThanOrEqualTo(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("());");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("}");
				method.addBodyLine(sb.toString());

				getMethod = getGetterMethodName(property+"_lte",fqjt);
				sb.setLength(0);
				sb.append("if(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("() != null) {");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("criteria.and").append(property).append("LessThanOrEqualTo(").append(getValidPropertyName(modelType.getShortName())).append(".").append(getMethod).append("());");
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append("}");
				method.addBodyLine(sb.toString());
			}
		}

		method.addBodyLine("return "+getValidPropertyName(exampleType.getShortName())+";");
		topLevelClass.addMethod(method);
	}
	private List<IntrospectedColumn> getColumnsInThisClass() {
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

	private boolean includePrimaryKeyColumns() {
		return !introspectedTable.getRules().generatePrimaryKeyClass() && introspectedTable.hasPrimaryKeyColumns();
	}

	private boolean includeBLOBColumns() {
		return !introspectedTable.getRules().generateRecordWithBLOBsClass() && introspectedTable.hasBLOBColumns();
	}


}
