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

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansGetter;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getValidPropertyName;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class BaseRecordDTOGenerator extends BaseRecordDTOBaseGenerator {

	public static final String VO_BASE_INTERFACE = "com.viontech.keliu.base.DTOInterface";
	
	public BaseRecordDTOGenerator(String project) {
		super(project);
	}
	
	
	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.8", table.toString()));
		Plugin plugins = context.getPlugins();
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordDTOType());
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);


		FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(introspectedTable.getBaseRecordDTOBaseType());
		if (superClass != null) {
			topLevelClass.setSuperClass(superClass);
			topLevelClass.addImportedType(superClass);
		}


		addDTOConstructor(topLevelClass);
		addGetMethod(topLevelClass);
		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}

	private void addDTOConstructor(TopLevelClass topLevelClass) {
		
		FullyQualifiedJavaType baseModelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

		Method method = new Method(topLevelClass.getType().getShortName());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		method.addBodyLine("super();");
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);


		method = new Method(topLevelClass.getType().getShortName());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		
		method.addParameter(new Parameter(baseModelType,getValidPropertyName(baseModelType.getShortName()) ));
		
		StringBuilder sb = new StringBuilder();
		sb.append("super(");
		sb.append(getValidPropertyName(baseModelType.getShortName()));
		sb.append(");");
		method.addBodyLine(sb.toString());

		topLevelClass.addImportedType(baseModelType);

		topLevelClass.addMethod(method);
	}

	private void addGetMethod(TopLevelClass topLevelClass) {
		List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();
		Plugin plugins = context.getPlugins();
		String rootClass = getRootClass();
		StringBuilder sb = new StringBuilder();
		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
				continue;
			}
			FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
			Method method = getJavaBeansGetter(introspectedColumn, context, introspectedTable);
			method.addAnnotation("@Override");
			if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
				method.getBodyLines().clear();
				sb.setLength(0);
				sb.append("return "); //$NON-NLS-1$
				sb.append("super.");
				sb.append(method.getName()).append("(");
				sb.append(")");
				sb.append(';');
				method.addBodyLine(sb.toString());

				if(needValidation(introspectedColumn)){
					String property = getValidPropertyName(topLevelClass.getType().getShortName());
					if(introspectedColumn.isJdbcCharacterColumn()){
						topLevelClass.addImportedType("javax.validation.constraints.NotBlank");
						String s = String.format("@NotBlank(message=\"{NotBlank.%s.%s}\",groups = {CreateAction.class})",property,introspectedColumn.getJavaProperty());
						method.addAnnotation(s);
					}else {
						topLevelClass.addImportedType("javax.validation.constraints.NotNull");
						String s = String.format("@NotNull(message=\"{NotNull.%s.%s}\",groups = {CreateAction.class})",property,introspectedColumn.getJavaProperty());
						method.addAnnotation(s);
					}
				}

				topLevelClass.addImportedType("com.github.sumanit.base.action.CreateAction");

				topLevelClass.addMethod(method);
			}

		}

	}
	private boolean needValidation(IntrospectedColumn introspectedColumn ){
		if(introspectedColumn.isNullable()){
			return false;
		}
		if(introspectedColumn.getIntrospectedTable().getPrimaryKeyColumns().contains(introspectedColumn)){
			return false;
		}
		if("createTime".equals(introspectedColumn.getJavaProperty())){
			return false;
		}
		if("modifyTime".equals(introspectedColumn.getJavaProperty())){
			return false;
		}
		return true;

	}

}
