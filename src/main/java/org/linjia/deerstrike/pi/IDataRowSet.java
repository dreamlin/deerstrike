package org.linjia.deerstrike.pi;

import java.util.List;

public interface IDataRowSet<T extends IDataRow> {
	List<T> getRows();	
	void add(T row);
}
