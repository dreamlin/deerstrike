package org.linjia.deerstrike.pi;

public interface IDataRow {
	boolean checkLoadState();

	void loadStateToTrue();

	void loadStateToFalse();
}
