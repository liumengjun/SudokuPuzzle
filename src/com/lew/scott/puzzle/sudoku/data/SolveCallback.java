package com.lew.scott.puzzle.sudoku.data;

public interface SolveCallback {
	public void iterateCallback(int itrTimes, boolean isAllSolved, boolean hasAchievement);
	
	public void solveCellCallback(int x, int y, int value);
	
	public void reduceCellCallback(int x, int y, int value);
}
