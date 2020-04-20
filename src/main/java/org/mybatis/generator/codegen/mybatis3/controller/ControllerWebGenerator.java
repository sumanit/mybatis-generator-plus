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

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getValidPropertyName;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class ControllerWebGenerator extends AbstractJavaGenerator {

	public ControllerWebGenerator(String project) {
		super(project);
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
		Plugin plugins = context.getPlugins();
		CommentGenerator commentGenerator = context.getCommentGenerator();
		FullyQualifiedJavaType baseControllerType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType().replace(".model", ".controller.base")+"BaseController");
		
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType().replace(".model", ".controller.web")+"Controller");
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		topLevelClass.setAbstract(false);
		commentGenerator.addJavaFileComment(topLevelClass);
		topLevelClass.addImportedType(baseControllerType);
		FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		topLevelClass.addImportedType("org.springframework.stereotype.Controller");
		topLevelClass.addImportedType("org.springframework.web.bind.annotation.RequestMapping");
		topLevelClass.addAnnotation("@Controller");
		topLevelClass.addAnnotation("@RequestMapping(\"/"+getValidPropertyName(modelType.getShortName())+(modelType.getShortName().endsWith("s")?"es":"s")+"\")");
		FullyQualifiedJavaType superClass = baseControllerType;
		FullyQualifiedJavaType serviceType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType().replaceAll("Mapper", "Service").replaceAll(".mapper", ".service.adapter"));
		if (superClass != null) {
			topLevelClass.setSuperClass(superClass);
			
			topLevelClass.addImportedType(superClass);
		}

		//createGetExampleMethod(topLevelClass);
		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
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
		sb.append(exampleType.getShortName()).append(" ").append(getValidPropertyName(exampleType.getShortName())).append(" = ").append("(").append(exampleType.getShortName()).append(")").append("super.getExample(").append(getValidPropertyName(modelType.getShortName())).append(",queryModel);");
		method.addBodyLine(sb.toString());
		method.addBodyLine("return "+getValidPropertyName(exampleType.getShortName())+";");
		topLevelClass.addMethod(method);
	}

}
