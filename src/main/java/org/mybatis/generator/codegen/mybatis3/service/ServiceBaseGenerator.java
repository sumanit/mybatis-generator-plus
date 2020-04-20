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

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.JavaBeansUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class ServiceBaseGenerator extends AbstractJavaGenerator {

	public ServiceBaseGenerator(String project) {
		super(project);
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
		Plugin plugins = context.getPlugins();
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType().replace(".model", ".service.base")+"ServiceBase");
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);
		topLevelClass.setAbstract(true);

		FullyQualifiedJavaType superClass = getSuperClass();
		FullyQualifiedJavaType superInterface = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType().replaceAll("Mapper", "Service").replaceAll(".mapper", ".service.adapter"));
		topLevelClass.addSuperInterface(superInterface);
		if (superClass != null) {
			topLevelClass.setSuperClass(superClass);
			
			topLevelClass.addImportedType(superInterface);
			topLevelClass.addImportedType(superClass);
		}

		FullyQualifiedJavaType mapper = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());

		topLevelClass.addImportedType( new FullyQualifiedJavaType("javax.annotation.Resource"));
		topLevelClass.addImportedType( new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
		topLevelClass.addImportedType(mapper);
		Field field = new Field(getValidPropertyName(mapper.getShortName()),mapper);
		field.addAnnotation("@Resource");
		field.setVisibility(JavaVisibility.PRIVATE);
		topLevelClass.addField(field);

		List<IntrospectedColumn> foreignKeyColumns = introspectedTable.getForeignKeyColumns();
		for (IntrospectedColumn foreignKeyColumn : foreignKeyColumns) {
			IntrospectedTable introspectedTablet = foreignKeyColumn.getIntrospectedImportColumn().getIntrospectedTable();
			FullyQualifiedJavaType importServiceType = new FullyQualifiedJavaType(introspectedTablet.getMyBatis3JavaMapperType().replaceAll("Mapper", "Service").replaceAll(".mapper", ".service.adapter"));
			field = new Field(getValidPropertyName(importServiceType.getShortName()),importServiceType);
			topLevelClass.addImportedType(importServiceType);
			field.addAnnotation("@Resource");
			field.setVisibility(JavaVisibility.PRIVATE);
			topLevelClass.addField(field);
		}



		Method method = new Method("getMapper");
		method.addAnnotation("@Override");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(false);

		FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(
                "com.github.sumanit.base.Mapper<"+introspectedTable.getBaseRecordType()+","+introspectedTable.getBaseRecordType()+"Example>");
		topLevelClass.addImportedType(returnType);
		method.setReturnType(returnType);
		method.addBodyLine("return "+getValidPropertyName(mapper.getShortName())+";");
		//method.addBodyLine("leftJoinTableSet = new HashSet<String>();");
		
		topLevelClass.addMethod(method);
		createBuildExampleMethod(topLevelClass);
		createBuildComplexProperty(topLevelClass);
		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}

	private FullyQualifiedJavaType getSuperClass() {
		FullyQualifiedJavaType superClass;
		String rootClass = "com.github.sumanit.base.BaseServiceImpl<"+introspectedTable.getBaseRecordType()+","+introspectedTable.getBaseRecordDTOType()+","+introspectedTable.getBaseRecordType()+"Example>";
		if (rootClass != null) {
			superClass = new FullyQualifiedJavaType(rootClass);
		} else {
			superClass = null;
		}

		return superClass;
	}

	private void createBuildExampleMethod(TopLevelClass topLevelClass){
		Method method = new Method("buildExample");
		method.addAnnotation("@Override");
		method.setVisibility(JavaVisibility.PUBLIC);
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
		/**
		if(dicDataDTO.getDicType()!=null){
			List<DicType> dicTypes = dicTypeService.selectByExample(dicTypeService.buildExample(dicDataDTO.getDicType(), QueryModel.SIMPLE));
			List<String> dicTypeIds = dicTypes.stream().map(DicType::getId).collect(Collectors.toList());
			dicDataExample.setComplexProperty(DicDataExample.FIELD_DICTYPE,dicTypes);
			dicDataDTO.setTypeId_arr(dicTypeIds);
		}
		**/
		List<IntrospectedColumn> foreignKeyColumns = introspectedTable.getForeignKeyColumns();
		if(foreignKeyColumns !=null && !foreignKeyColumns.isEmpty()) {
			topLevelClass.addImportedType("java.util.List");
			topLevelClass.addImportedType("java.util.stream.Collectors");
		}
		for (IntrospectedColumn foreignKeyColumn : foreignKeyColumns) {
			IntrospectedColumn introspectedImportColumn = foreignKeyColumn.getIntrospectedImportColumn();
			IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
			FullyQualifiedJavaType importTableType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType());
			topLevelClass.addImportedType(importTableType);
			FullyQualifiedJavaType importServiceType = new FullyQualifiedJavaType(introspectedImportTable.getMyBatis3JavaMapperType().replaceAll("Mapper", "Service").replaceAll(".mapper", ".service.adapter"));
			String methodLine = String.format("if(%s.%s()!=null){",getValidPropertyName(modelType.getShortName()),getGetterMethodName(getValidPropertyName(importTableType.getShortName()),importTableType));
			method.addBodyLine(methodLine);
			//List<DicType> dicTypes = dicTypeService.selectByExample(dicTypeService.buildExample(dicDataDTO.getDicType(), QueryModel.SIMPLE));
			methodLine = String.format("List<%s> %ss = %s.selectByExample(%s.buildExample(%s.%s(), QueryModel.SIMPLE));",
							importTableType.getShortName(),
							getValidPropertyName(importTableType.getShortName()),
							getValidPropertyName(importServiceType.getShortName()),
							getValidPropertyName(importServiceType.getShortName()),
							getValidPropertyName(modelType.getShortName()),
							getGetterMethodName(getValidPropertyName(importTableType.getShortName()),importTableType)

						);
			method.addBodyLine(methodLine);
			//List<String> dicTypeIds = dicTypes.stream().map(DicType::getId).collect(Collectors.toList());
			methodLine = String.format("List<String> %sIds = %ss.stream().map(%s::%s).collect(Collectors.toList());",
					getValidPropertyName(importTableType.getShortName()),
					getValidPropertyName(importTableType.getShortName()),
					importTableType.getShortName(),
					getGetterMethodName(introspectedImportColumn.getJavaProperty(),introspectedImportColumn.getFullyQualifiedJavaType())

			);
			method.addBodyLine(methodLine);
			// dicDataExample.setComplexProperty(DicDataExample.FIELD_DICTYPE,dicTypes);
			methodLine = String.format("%s.setComplexProperty(%s.FIELD_%s,%ss);",
					getValidPropertyName(exampleType.getShortName()),
					exampleType.getShortName(),
					importTableType.getShortName().toUpperCase(),
					getValidPropertyName(importTableType.getShortName())
			);
			method.addBodyLine(methodLine);

			// dicDataDTO.setTypeId_arr(dicTypeIds);
			methodLine = String.format("%s.%s_arr(%sIds);",
					getValidPropertyName(modelType.getShortName()),
					getSetterMethodName(foreignKeyColumn.getJavaProperty()),
					getValidPropertyName(importTableType.getShortName())
			);
			method.addBodyLine(methodLine);
			method.addBodyLine("}");
		}


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


	/**
	 *
	 */
	private void createBuildComplexProperty(TopLevelClass topLevelClass){

		Method method = new Method("buildComplexProperty");
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()); // DicData
		FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType()); //DicDataExample
		String modelProperty = getValidPropertyName(modelType.getShortName()); // dicData
		String exampleProperty = getValidPropertyName(exampleType.getShortName()); // dicDataExample
		method.addParameter(new Parameter(exampleType,exampleProperty));
		topLevelClass.addImportedType("java.util.List");
		method.addParameter(new Parameter(new FullyQualifiedJavaType("List<"+modelType.getShortName()+">"),modelProperty+"s"));
		List<IntrospectedColumn> foreignKeyColumns = introspectedTable.getForeignKeyColumns();
		for (IntrospectedColumn foreignKeyColumn : foreignKeyColumns) {
			// dic_type.id
			IntrospectedColumn introspectedImportColumn = foreignKeyColumn.getIntrospectedImportColumn();
			// dic_type
			IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
			// DicType
			FullyQualifiedJavaType importTableType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType());
			// DicTypeService
			FullyQualifiedJavaType importServiceType = new FullyQualifiedJavaType(introspectedImportTable.getMyBatis3JavaMapperType().replaceAll("Mapper", "Service").replaceAll(".mapper", ".service.adapter"));
			//List<String> typeIds = results.stream().map(DicData::getTypeId).collect(Collectors.toList());
			String methodBodyLine = String.format("List<%s> %ss = %ss.stream().map(%s::%s).filter(item->item!=null).collect(Collectors.toList());",
					foreignKeyColumn.getFullyQualifiedJavaType().getShortName(),
					foreignKeyColumn.getJavaProperty(),
					modelProperty,
					modelType.getShortName(),
					getGetterMethodName(foreignKeyColumn.getJavaProperty(),foreignKeyColumn.getFullyQualifiedJavaType())
			);
			method.addBodyLine(methodBodyLine);
			// if(typeIds != null && !typeIds.isEmpty()){
			methodBodyLine = String.format("if(%ss != null && !%ss.isEmpty()){",foreignKeyColumn.getJavaProperty(),foreignKeyColumn.getJavaProperty());
			method.addBodyLine(methodBodyLine);
			//DicTypeDTO dicTypeDTO = new DicTypeDTO();
			FullyQualifiedJavaType importDtoType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordDTOType());
			topLevelClass.addImportedType(importDtoType);
			methodBodyLine = String.format("%s %s = new %s();",
					importDtoType.getShortName(),
					getValidPropertyName(importDtoType.getShortName()),
					importDtoType.getShortName()
				);
			method.addBodyLine(methodBodyLine);

			///dicTypeDTO.setId_arr(typeIds);
			methodBodyLine = String.format("%s.%s_arr(%ss);",
					getValidPropertyName(importDtoType.getShortName()),
					getSetterMethodName(introspectedImportColumn.getJavaProperty()),
					foreignKeyColumn.getJavaProperty()
			);
			method.addBodyLine(methodBodyLine);

			// List<DicType> dicTypes = dicTypeService.selectByExample(dicTypeService.buildExample(dicTypeDTO, QueryModel.MEDIUM));
			methodBodyLine = String.format("List<%s> %ss = %s.selectByExample(%s.buildExample(%s, QueryModel.MEDIUM));",
					importTableType.getShortName(),
					getValidPropertyName(importTableType.getShortName()),
					getValidPropertyName(importServiceType.getShortName()),
					getValidPropertyName(importServiceType.getShortName()),
					getValidPropertyName(importDtoType.getShortName()),
					foreignKeyColumn.getJavaProperty()
			);
			method.addBodyLine(methodBodyLine);

			// dicDataExample.setComplexProperty(DicDataExample.FIELD_DICTYPE,dicTypes);
			methodBodyLine = String.format("%s.setComplexProperty(%s.FIELD_%s,%ss);",
					exampleProperty,
					exampleType.getShortName(),
					importTableType.getShortName().toUpperCase(),
					getValidPropertyName(importTableType.getShortName())
			);
			method.addBodyLine(methodBodyLine);
			method.addBodyLine("}");
		}
		topLevelClass.addMethod(method);



		/*@Override
		public void buildComplexProperty(DicDataExample baseExample, List<DicData> results) {
			 List<String> typeIds = results.stream().map(DicData::getTypeId).filter(item->item!=null).collect(Collectors.toList());
			if(typeIds != null && !typeIds.isEmpty()){
				DicTypeDTO dicTypeDTO = new DicTypeDTO();
				dicTypeDTO.setId_arr(typeIds);
				List<DicType> dicTypes = dicTypeService.selectByExample(dicTypeService.buildExample(dicTypeDTO, QueryModel.MEDIUM));
				baseExample.setComplexProperty(DicDataExample.FIELD_DICTYPE,dicTypes);
			}
		}*/
	}

}
