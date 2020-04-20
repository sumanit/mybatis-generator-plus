package org.mybatis.generator.api;

import org.mybatis.generator.internal.db.ActualTableName;

public class ImportColumn{
    protected ActualTableName importTable;
    protected String importColumnName;

    public ImportColumn(ActualTableName importTable, String importColumnName) {
		super();
		this.importTable = importTable;
		this.importColumnName = importColumnName;
    }
	public ActualTableName getImportTable() {
		return importTable;
	}

	public void setImportTable(ActualTableName importTable) {
		this.importTable = importTable;
	}

	public String getImportColumnName() {
		return importColumnName;
	}

	public void setImportColumnName(String importColumnName) {
		this.importColumnName = importColumnName;
	}

}