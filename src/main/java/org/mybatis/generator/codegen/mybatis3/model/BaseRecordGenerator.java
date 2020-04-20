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

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.Context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class BaseRecordGenerator extends AbstractJavaGenerator {

    public BaseRecordGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);

        FullyQualifiedJavaType superClass = getSuperClass();
        if (superClass != null) {
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
        }
        FullyQualifiedJavaType rootInterface = new FullyQualifiedJavaType("com.github.sumanit.base.Model");
        if (rootInterface != null) {
            topLevelClass.addSuperInterface(rootInterface);
            topLevelClass.addImportedType(rootInterface);
        }

        commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();

        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass, introspectedTable.getNonBLOBColumns());

            if (includeBLOBColumns()) {
                addParameterizedConstructor(topLevelClass, introspectedTable.getAllColumns());
            }

            if (!introspectedTable.isImmutable()) {
                addDefaultConstructor(topLevelClass);
            }
        }

        String rootClass = getRootClass();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
                continue;
            }

            Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
            if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
                String annotation = "@ApiParam(value = \"%s(%s) 等于该值\")";
                annotation = String.format(annotation, introspectedColumn.getRemarks()==null?introspectedColumn.getJavaProperty():introspectedColumn.getRemarks(),introspectedColumn.getJavaProperty());
                field.addAnnotation(annotation);
                topLevelClass.addImportedType("io.swagger.annotations.ApiParam");

                annotation = String.format("@ApiModelProperty(value=\"%s\" %s)",introspectedColumn.getRemarks(),isRequired(introspectedColumn)?",required = true":"");
                field.addAnnotation(annotation);
                topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
            }

            Method method = getJavaBeansGetter(introspectedColumn, context, introspectedTable);
            if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addMethod(method);
            }

            if (!introspectedTable.isImmutable()) {
                method = getJavaBeansSetter(introspectedColumn, context, introspectedTable);
                if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                    topLevelClass.addMethod(method);
                }
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
            // 外键表
            IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
            // 外键类型
            FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType());

            Field field = new Field(getValidPropertyName(importType.getShortName()),importType);
            field.setVisibility(JavaVisibility.PRIVATE);
            context.getCommentGenerator().addFieldComment(field, introspectedTable, introspectedColumn);
            if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
            }

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
        List<CompilationUnit> answer = new ArrayList<>();
        if (context.getPlugins().modelBaseRecordClassGenerated(
                topLevelClass, introspectedTable)) {
            answer.add(topLevelClass);
        }
        return answer;
    }

    private FullyQualifiedJavaType getSuperClass() {
        FullyQualifiedJavaType superClass;
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            superClass = new FullyQualifiedJavaType(introspectedTable
                    .getPrimaryKeyType());
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
        return !introspectedTable.getRules().generatePrimaryKeyClass()
                && introspectedTable.hasPrimaryKeyColumns();
    }

    private boolean includeBLOBColumns() {
        return !introspectedTable.getRules().generateRecordWithBLOBsClass()
                && introspectedTable.hasBLOBColumns();
    }

    private void addParameterizedConstructor(TopLevelClass topLevelClass, List<IntrospectedColumn> constructorColumns) {
        Method method = new Method(topLevelClass.getType().getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(),
                    introspectedColumn.getJavaProperty()));
            topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
        }

        StringBuilder sb = new StringBuilder();
        List<String> superColumns = new LinkedList<>();
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            boolean comma = false;
            sb.append("super("); //$NON-NLS-1$
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                if (comma) {
                    sb.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }
                sb.append(introspectedColumn.getJavaProperty());
                superColumns.add(introspectedColumn.getActualColumnName());
            }
            sb.append(");"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        }

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            if (!superColumns.contains(introspectedColumn.getActualColumnName())) {
                sb.setLength(0);
                sb.append("this."); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" = "); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(';');
                method.addBodyLine(sb.toString());
            }
        }

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
                introspectedColumns = introspectedTable
                        .getNonPrimaryKeyColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }
        }

        return introspectedColumns;
    }

    // add by suman start
    private Method getImportJavaBeansSetter(IntrospectedColumn introspectedColumn, Context context) {
        IntrospectedColumn introspectedImportColumn = introspectedColumn.getIntrospectedImportColumn();
        if(introspectedImportColumn == null ){
            return null;
        }

        IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
        FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType());
        String property = getValidPropertyName(importType.getShortName());

        Method method = new Method(getSetterMethodName(property));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(importType, property));
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

    private Method getImportJavaBeansGetter(IntrospectedColumn introspectedColumn, Context context) {
        IntrospectedColumn introspectedImportColumn = introspectedColumn.getIntrospectedImportColumn();
        if(introspectedImportColumn == null ){
            return null;
        }

        IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
        FullyQualifiedJavaType importType = new FullyQualifiedJavaType(introspectedImportTable.getBaseRecordType());
        String property = getValidPropertyName(importType.getShortName());

        Method method = new Method(getGetterMethodName(property, importType));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(importType);
        context.getCommentGenerator().addGetterComment(method, introspectedImportTable, introspectedColumn);

        StringBuilder sb = new StringBuilder();
        sb.append("return "); //$NON-NLS-1$
        sb.append(property);
        sb.append(';');
        method.addBodyLine(sb.toString());

        return method;
    }
    private boolean isRequired(IntrospectedColumn introspectedColumn ){
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
    // add by suman end
}
