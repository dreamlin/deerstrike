package org.linjia.deerstrike.base;

import org.linjia.deerstrike.pi.IRowPager;

public class RowPagerBase implements IRowPager {
	private int	pageIndex	= 1;	// 页索引
	private int	pageSize	= 20;	// 页大小
	private int	hitRows;			// 查询命中的行数
	private int	maxLoop;			// 最大需要枚举的行数

	public RowPagerBase() {
		this.maxLoop = this.pageSize * 20;
	}

	/**
	 * @return the pageIndex
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * @param pageIndex
	 *        the pageIndex to set
	 */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize
	 *        the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the hitRows
	 */
	public int getHitRows() {
		return hitRows;
	}

	/**
	 * @param hitRows
	 *        the hitRows to set
	 */
	public void setHitRows(int hitRows) {
		this.hitRows = hitRows;
	}

	/**
	 * @return the maxLoopRows
	 */
	public int getMaxLoop() {
		return maxLoop;
	}

	/**
	 * @param maxLoop
	 */
	public void setMaxLoop(int maxLoop) {
		this.maxLoop = maxLoop;
	}

	public int getBeginIx() {
		return (this.pageIndex - 1) * this.pageSize;
	}

	public int getEndIx() {
		if (this.pageSize == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;

		return this.pageIndex * this.pageSize;
	}

	public IRowPager selectAllQueryPager() {
		RowPagerBase rowPagerBase = new RowPagerBase();
		rowPagerBase.setPageSize(Integer.MAX_VALUE);
		rowPagerBase.setMaxLoop(Integer.MAX_VALUE);
		return rowPagerBase;
	}
}
