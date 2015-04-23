package org.linjia.deerstrike.pi;

public interface IRowPager {
	int getPageIndex();

	void setPageIndex(int pageIndex);

	int getPageSize();

	void setPageSize(int pageSize);

	int getHitRows();

	void setHitRows(int hitRows);

	int getMaxLoop();

	void setMaxLoop(int maxLoop);

	int getBeginIx();

	int getEndIx();

	IRowPager selectAllQueryPager();
}
