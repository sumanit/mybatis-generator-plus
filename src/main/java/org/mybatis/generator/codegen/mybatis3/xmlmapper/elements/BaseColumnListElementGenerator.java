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
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.Iterator;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class BaseColumnListElementGenerator extends AbstractXmlElementGenerator {

	public BaseColumnListElementGenerator() {
		super();
	}

	@Override
	public void addElements(XmlElement parentElement) {
		XmlElement answer = new XmlElement("sql");

		answer.addAttribute(new Attribute("id", introspectedTable.getBaseColumnListId()));

		context.getCommentGenerator().addComment(answer);
		XmlElement ifExample  = new XmlElement("if");
		StringBuffer sb = new StringBuffer();
		sb.append("!(_parameter.getClass().getSimpleName() == '");
		sb.append(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
		sb.append("Example')");
		ifExample.addAttribute(new Attribute("test", sb.toString()));
		answer.addElement(ifExample);
		
		XmlElement include = new XmlElement("include");
		include.addAttribute(new Attribute("refid",introspectedTable.getMyBatis3SqlMapNamespace()+"."+introspectedTable.getBaseColumnListRootId()));
		ifExample.addElement(include);

		
		XmlElement elseIfExample  = new XmlElement("if");
		sb.setLength(0);
		sb.append("_parameter.getClass().getSimpleName() == '");
		sb.append(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
		sb.append("Example'");
		elseIfExample.addAttribute(new Attribute("test", sb.toString()));
		answer.addElement(elseIfExample);
		
		
		
		
		XmlElement foreachElement = new XmlElement("foreach");
		elseIfExample.addElement(foreachElement);
		foreachElement.addAttribute(new Attribute("collection", "columnContainerSet"));
		foreachElement.addAttribute(new Attribute("item", "columns"));
		foreachElement.addAttribute(new Attribute("separator", ","));
		
		XmlElement chooseElement = new XmlElement("choose");
		foreachElement.addElement(chooseElement);
		
		
		chooseElement.addElement(getWhenElement(introspectedTable));
		Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns().iterator();
		while (iter.hasNext()) {
			IntrospectedColumn column = iter.next();
			IntrospectedColumn introspectedImportColumn = column.getIntrospectedImportColumn();
			if(introspectedImportColumn==null){
				continue;
			}
			IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
			if(introspectedImportTable.equals(introspectedTable)){
				continue;
			}
			XmlElement whenElement = getWhenElement(introspectedImportTable);
			chooseElement.addElement(whenElement);
			

		}

		
		
		
		if (context.getPlugins().sqlMapBaseColumnListElementGenerated(answer, introspectedTable)) {
			parentElement.addElement(answer);
		}
	}
	
	private XmlElement getWhenElement(IntrospectedTable table){
		XmlElement whenElement= new XmlElement("when");
		StringBuffer sb = new StringBuffer();
		sb.setLength(0);
		sb.append("columns.tableName == '");
		sb.append(table.getFullyQualifiedTable().getIntrospectedTableName());
		sb.append("'.toString()");
		
		whenElement.addAttribute(new Attribute("test", sb.toString()));
		
		XmlElement ifElement = new XmlElement("if");
		ifElement.addAttribute(new Attribute("test", "columns.valid"));
		ifElement.addElement(new TextElement("${columns.columnContainerStr}"));
		whenElement.addElement(ifElement);
		XmlElement elseIfElement = new XmlElement("if");
		elseIfElement.addAttribute(new Attribute("test", "!columns.valid"));
		XmlElement includeElement = new XmlElement("include");
		includeElement.addAttribute(new Attribute("refid", table.getMyBatis3SqlMapNamespace()+"."+table.getBaseColumnListRootId()));
		elseIfElement.addElement(includeElement);
		whenElement.addElement(elseIfElement);
		return whenElement;
	}
}
