package org.linjia.deerstrike.base;

import java.util.ArrayList;
import java.util.List;

public class DataRowBaseSet extends DataRowSetBase<DataRowBase> {
	private List<DataRowBase>	rows;

	public DataRowBaseSet() {
		this.rows = new ArrayList<DataRowBase>();
	}

	public void add(DataRowBase dataRow) {
		this.rows.add(dataRow);
	}

	public List<DataRowBase> getRows() {
		return this.rows;
	}
}
