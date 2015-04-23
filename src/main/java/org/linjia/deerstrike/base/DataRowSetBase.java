package org.linjia.deerstrike.base;

import java.util.List;

import org.linjia.deerstrike.pi.IDataRow;
import org.linjia.deerstrike.pi.IDataRowSet;

public class DataRowSetBase<T extends IDataRow> implements IDataRowSet<T> {

	public List<T> getRows() {
		return null;
	}

	public void add(T row) {
	}
}