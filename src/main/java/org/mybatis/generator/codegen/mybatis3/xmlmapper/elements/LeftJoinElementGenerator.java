package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.List;

//add by suman
public class LeftJoinElementGenerator extends AbstractXmlElementGenerator {

	@Override
	public void addElements(XmlElement parentElement) {
		XmlElement answer = new XmlElement("sql");
		answer.addAttribute(new Attribute("id", introspectedTable.getLeftJoinListId()));

		List<IntrospectedColumn> columns = introspectedTable.getBaseColumns();
/*
		XmlElement ifElement = new XmlElement("if");
		ifElement.addAttribute(new Attribute("test", "leftJoinTableSet.size()==0"));
		answer.addElement(ifElement);
		
		XmlElement elseIfElement = new XmlElement("if");
		elseIfElement.addAttribute(new Attribute("test", "leftJoinTableSet.size()!=0"));
		answer.addElement(elseIfElement);*/
		XmlElement foreach = new XmlElement("foreach");
		//elseIfElement.addElement(foreach);
		answer.addElement(foreach);
		foreach.addAttribute(new Attribute("collection", "leftJoinTableSet"));
		foreach.addAttribute(new Attribute("item", "leftJoinTable"));
		XmlElement choose = new XmlElement("choose");
		foreach.addElement(choose);
		boolean isNeedAdd = false;
		for (IntrospectedColumn introspectedColumn : columns) {

			IntrospectedColumn introspectedImportColumn = introspectedColumn.getIntrospectedImportColumn();
			if (introspectedImportColumn == null) {
				continue;
			}
			IntrospectedTable introspectedImportTable = introspectedImportColumn.getIntrospectedTable();
			if(introspectedImportTable.equals(introspectedTable)){
				continue;
			}
			isNeedAdd = true;
			StringBuffer sb = new StringBuffer();
			sb.append("left join ");
			sb.append(introspectedImportTable.getAliasedFullyQualifiedTableNameAtRuntime());
			sb.append(" on ");
			sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedImportColumn));
			sb.append(" = ");
			sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
			
			
			//ifElement.addElement(new TextElement(sb.toString()));
			
			XmlElement when = new XmlElement("when");
			when.addElement(new TextElement(sb.toString()));
			sb.setLength(0);
			sb.append("leftJoinTable == '");
			sb.append(introspectedImportTable.getFullyQualifiedTable().getIntrospectedTableName());
			sb.append("'.toString()");
			when.addAttribute(new Attribute("test", sb.toString()));
			choose.addElement(when);

		}
		if(answer.getElements() == null || answer.getElements().isEmpty()||!isNeedAdd){
			return;
		}
		parentElement.addElement(answer);
		context.getCommentGenerator().addComment(answer);

	}

}
