package com.lew.scott.puzzle.sudoku.data;

import java.util.ArrayList;

public class Cell {
	private int value; // 当前数独总单元格中的数字，0为空
	private boolean preset; // 是否是预设定的，如果不是则需要求解。
	private boolean solved; // 是否已经被解决
	private ArrayList<Integer> optValueList; // 如果不是预设定的，可选值列表

	public Cell() {

	}

	/**
	 * 根据给定的数字初始化
	 * 
	 * @param value
	 */
	public Cell(int value) {
		if (value < 1 || value > SudokuMatrix.SQUARE_LENGTH) {
			this.value = SudokuMatrix.UNSET_VALUE;
			this.preset = false;
		} else {
			this.value = value;
			this.preset = true;
		}
	}

	/**
	 * 以给定值初始化cell，用于解决数独谜题{@link SudokuMatrix#solve()}
	 * 
	 * @param value
	 */
	public void initByValue(int value) {
		if (value < 1 || value > SudokuMatrix.SQUARE_LENGTH) {
			this.value = SudokuMatrix.UNSET_VALUE;
			this.preset = false;
		} else {
			this.value = value;
			this.preset = true;
		}
		this.optValueList = null;
		this.solved = false;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isPreset() {
		return preset;
	}

	public void setPreset(boolean preset) {
		this.preset = preset;
	}

	public boolean isSolved() {
		return solved;
	}

	public void setSolved(boolean solved) {
		this.solved = solved;
	}

	public ArrayList<Integer> getOptValueList() {
		return optValueList;
	}

	public void setOptValueList(ArrayList<Integer> optValueList) {
		this.optValueList = optValueList;
	}

	/**
	 * 从可选值列表中选出一个值，用于生成数独矩阵{@link SudokuMatrix#generateValidMatrix()}
	 * 
	 * @return
	 */
	public int pickOneOptValue() {
		if (this.optValueList != null) {
			return this.optValueList.remove(this.optValueList.size() - 1);
		}
		return SudokuMatrix.UNSET_VALUE;
	}

	/**
	 * 检测可选值列表是否为空，用于生成数独矩阵{@link SudokuMatrix#generateValidMatrix()}
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return (this.optValueList == null || this.optValueList.isEmpty());
	}

	/**
	 * 清空可选值列表，用于生成数独矩阵{@link SudokuMatrix#generateValidMatrix()}
	 */
	public void clear() {
		if (this.optValueList != null) {
			this.optValueList.clear();
		}
	}

	/**
	 * 从可选值列表中移除某个值，用于解决数独谜题{@link SudokuMatrix#solve()}
	 * 
	 * @param v
	 */
	public void remove(int v) {
		if (this.optValueList != null) {
			this.optValueList.remove(new Integer(v));
		}
	}

	/**
	 * 检测该单元格是否已经被解决，用于解决数独谜题{@link SudokuMatrix#solve()}
	 * 
	 * @return
	 */
	public boolean checkIsSolved() {
		if (this.optValueList != null) {
			// 当可选值只有一个时，标记为已解决
			if (this.optValueList.size() == 1) {
				this.value = this.optValueList.get(0);
				this.solved = true;
				this.optValueList = null;
				return true;
			}
			return false;
		}
		return (value != SudokuMatrix.UNSET_VALUE);
	}

	/**
	 * 用给定的值解决该单元格，用于解决数独谜题{@link SudokuMatrix#solve()}
	 * 
	 * @param value
	 */
	public void solveByValue(int value) {
		if (value >= 1 && value <= SudokuMatrix.SQUARE_LENGTH) {
			this.value = value;
			this.solved = true;
			this.optValueList = null;
		}
	}

	public void unset() {
		this.value = SudokuMatrix.UNSET_VALUE;
		this.preset = false;
		this.optValueList = null;
		this.solved = false;
	}
}
