package com.lew.scott.puzzle.sudoku.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class SudokuMatrix {
	public static boolean DEBUG = false;

	public static final int SQUARE_LENGTH = 9; // 数独矩阵边长
	public static final int SQUARE_ROOT = 3; // 数独矩阵边长平方根
	public static final int UNSET_VALUE = 0;

	public static final int MIN_DEGREE = 1;
	public static final int MAX_DEGREE = 18;
	public static final int DEFAULT_DEGREE = 3;

	private int degreeOfPuzzle = DEFAULT_DEGREE;
	private int[][] matrix; // 数独矩阵上的数字
	private int[][] validMatrix;
	private Cell[][] cells; // 数独矩阵上的单元格

	public SudokuMatrix() {
		matrix = new int[SQUARE_LENGTH][SQUARE_LENGTH];
		cells = new Cell[SQUARE_LENGTH][SQUARE_LENGTH];
	}

	public SudokuMatrix(int degree) {
		this();
		setDegreeOfPuzzle(degree);
	}

	/**
	 * 根据给定的数字初始化数独矩阵
	 * 
	 * @param values
	 * @throws IllegalArgumentException
	 */
	public SudokuMatrix(int[][] values) throws IllegalArgumentException {
		this();
		initMatrix(values);
	}

	/**
	 * 根据给定的数字初始化数独矩阵
	 * 
	 * @param values
	 * @throws IllegalArgumentException
	 */
	public void initMatrix(int[][] values) throws IllegalArgumentException {
		if (values == null || values.length != SQUARE_LENGTH) {
			throw new IllegalArgumentException("初始化数独矩阵的参数错误");
		}
		for (int i = 0; i < values.length; i++) {
			if (values[i].length != SQUARE_LENGTH) {
				throw new IllegalArgumentException("初始化数独矩阵的参数错误");
			}
		}
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (values[i][j] < 1 || values[i][j] > SQUARE_LENGTH) {
					matrix[i][j] = UNSET_VALUE;
				} else {
					matrix[i][j] = values[i][j];
				}
			}
		}
		amendMatrix();
		solveInit();// 初始化cells
	}

	/**
	 * 检验一个不完全矩阵是否是一个合法的矩阵，如果含有重复数字则修正
	 * 
	 * @return
	 */
	private void amendMatrix() {
		// 用数值集合检测
		HashSet<Integer> numSet = new HashSet<Integer>();
		// 横向检测
		for (int i = 0; i < SQUARE_LENGTH; i++) {
			numSet.clear();
			for (int t = 0; t < matrix[i].length; t++) {
				int v = matrix[i][t];
				if (v < 1 || v > SQUARE_LENGTH) {// 忽略待求解的
					continue;
				}
				if (numSet.contains(v)) {
					matrix[i][t] = UNSET_VALUE;
					continue;
				}
				numSet.add(v);
			}
		}
		// 纵向检测
		for (int j = 0; j < SQUARE_LENGTH; j++) {
			numSet.clear();
			for (int s = 0; s < matrix.length; s++) {
				int v = matrix[s][j];
				if (v < 1 || v > SQUARE_LENGTH) {// 忽略待求解的
					continue;
				}
				if (numSet.contains(v)) {
					matrix[s][j] = UNSET_VALUE;
					continue;
				}
				numSet.add(v);
			}
		}
		// 3*3小矩阵检测
		for (int i = 0; i < matrix.length; i += SQUARE_ROOT) {
			for (int j = 0; j < matrix[i].length; j += SQUARE_ROOT) {
				numSet.clear();
				// 当同一3*3矩阵中出现重复数字时
				for (int s = i; s < i + SQUARE_ROOT; s++) {
					for (int t = j; t < j + SQUARE_ROOT; t++) {
						int v = matrix[s][t];
						if (v < 1 || v > SQUARE_LENGTH) {// 忽略待求解的
							continue;
						}
						if (numSet.contains(v)) {
							matrix[s][t] = UNSET_VALUE;
							continue;
						}
						numSet.add(v);
					}
				}
			}
		}
	}

	/**
	 * 返回当前数独矩阵
	 * 
	 * @return
	 */
	public int[][] getMatrix() {
		return cloneMatrix(matrix);
	}

	/**
	 * 得到和当前数独谜题矩阵对应的结果矩阵
	 * 
	 * @return
	 */
	public int[][] getAnswerMatrix() {
		if (validMatrix == null)
			return null;
		return cloneMatrix(validMatrix);
	}

	/**
	 * 返回numbers的克隆矩阵
	 * 
	 * @return
	 */
	private int[][] cloneMatrix(int[][] srcMatrix) {
		if (srcMatrix == null) {
			srcMatrix = this.matrix;
		}
		int[][] aa = new int[SQUARE_LENGTH][SQUARE_LENGTH];
		for (int i = 0; i < SQUARE_LENGTH; i++) {
			System.arraycopy(srcMatrix[i], 0, aa[i], 0, SQUARE_LENGTH);
		}
		return aa;
	}

	public int getDegreeOfPuzzle() {
		return degreeOfPuzzle;
	}

	public void setDegreeOfPuzzle(int degree) {
		if (degree < MIN_DEGREE || degree > MAX_DEGREE) {
			degree = DEFAULT_DEGREE;
		}
		this.degreeOfPuzzle = degree;
	}

	/**
	 * 生成一个数独谜题矩阵
	 * 
	 * @return
	 */
	public int[][] generatePuzzle() {
		this.validMatrix = this.generateValidMatrix();
		Random rand = new Random();
		for (int i = 0; i < matrix.length; i++) {
			for (int k = 0; k < degreeOfPuzzle; k++) {
				rand.setSeed(System.nanoTime());
				int iRnd = rand.nextInt(SQUARE_LENGTH);
				matrix[i][iRnd] = UNSET_VALUE;
			}
		}
		solveInit();// 初始化cells
		return cloneMatrix(matrix);
	}

	/**
	 * 生成一个合法的数独矩阵
	 * 
	 * @return
	 */
	public int[][] generateValidMatrix() {
		// clear all numbers and cells
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				matrix[i][j] = UNSET_VALUE;
				Cell c = cells[i][j];
				if (c == null) {
					cells[i][j] = c = new Cell();
				} else {
					c.unset();
				}
			}
		}
		ArrayList<Integer> numList = new ArrayList<Integer>();
		for (int i = 1; i <= SQUARE_LENGTH; i++) {
			numList.add(i);
		}
		// 首先用乱序的 [1 ~ SQUARE_LENGTH] 数字初始化第一行
		Collections.shuffle(numList);
		for (int j = 0; j < matrix[0].length; j++) {
			matrix[0][j] = numList.get(j);
			cells[0][j].initByValue(numList.get(j));
		}
		// 初始化第一列
		// numList.remove(new Integer(numbers[0][0]));
		// Collections.shuffle(numList);
		// for (int i = 1; i < numbers.length; i++) {
		// numbers[i][0] = numList.get(i - 1);
		// cells[i][0] = new Cell(numList.get(i - 1));
		// }
		if (DEBUG) {
			printMatrix(matrix);
		}
		// 生成未填充的单元格数字
		int x = 0, y = 0;
		boolean isForward = true;
		while (x < SQUARE_LENGTH || y < SQUARE_LENGTH) {
			Cell c = cells[x][y];
			if (matrix[x][y] < 1 || matrix[x][y] > SQUARE_LENGTH) {
				if (c == null) {
					cells[x][y] = c = new Cell();
				}
				if (c.isEmpty()) {// 检测这个单元格的可选值列表是否为空
					if (!isForward) { // 回退回来的情况，继续回退
						// goto previous cell of (x,y)
						Coord pc = prevCoord(x, y);
						x = pc.x;
						y = pc.y;
						isForward = false;
						if (DEBUG) {
							System.out.println("p x,y = " + x + "," + y);
							System.out.println(cells[x][y].getOptValueList());
						}
						if (cells[x][y] != null && cells[x][y].isPreset()) {
							if (DEBUG) {
								printMatrix(matrix);
							}
							throw new RuntimeException("逻辑错误");// 随机产生数独，有失败的情况
						} else {
							matrix[x][y] = UNSET_VALUE;// 清空上一个单元格的值
						}
						continue;
					}
					// 计算这个单元格中可选值列表
					numList = calcOptValueList(x, y, true);
					c.setOptValueList(numList);
				}
				if (!c.isEmpty()) {// 可选值列表非空，从可选值列表中取一个
					matrix[x][y] = c.pickOneOptValue();
					if (x == (SQUARE_LENGTH - 1) && y == (SQUARE_LENGTH - 1)) {
						break;
					}
					// goto next cell of (x,y)
					Coord nc = nextCoord(x, y);
					x = nc.x;
					y = nc.y;
					isForward = true;
					if (DEBUG) {
						System.out.println("n x,y = " + x + "," + y);
					}
				} else {// 没有可选值，回退到上一个单元格
					if (x == 0 && y == 0) {
						break;
					}
					// goto previous cell of (x,y)
					Coord pc = prevCoord(x, y);
					x = pc.x;
					y = pc.y;
					isForward = false;
					if (DEBUG) {
						System.out.println("p x,y = " + x + "," + y);
						System.out.println(cells[x][y].getOptValueList());
					}
					if (cells[x][y] != null && cells[x][y].isPreset()) {
						if (DEBUG) {
							printMatrix(matrix);
						}
						throw new RuntimeException("逻辑错误");// 随机产生数独，有失败的情况
					} else {
						matrix[x][y] = UNSET_VALUE;// 清空上一个单元格的值
					}
				}
			} else {
				// 当numbers[x][y]的值是预先设定好的情况下，直接进行下一个
				// goto next cell of (x,y)
				Coord nc = nextCoord(x, y);
				x = nc.x;
				y = nc.y;
				isForward = true;
				if (DEBUG) {
					System.out.println("n x,y = " + x + "," + y);
				}
			}
		}
		// 返回克隆的数据，保护内部数据
		return cloneMatrix(matrix);
	}

	private Coord nextCoord(int x, int y) {
		Coord cd = new Coord();
		if (y < (SQUARE_LENGTH - 1)) {
			cd.x = x;
			cd.y = y + 1;
			return cd;
		} else {
			if (x < (SQUARE_LENGTH - 1)) {
				cd.x = x + 1;
				cd.y = 0;
				return cd;
			}
		}
		return null;
	}

	private Coord prevCoord(int x, int y) {
		Coord cd = new Coord();
		if (y > 0) {
			cd.x = x;
			cd.y = y - 1;
			return cd;
		} else {
			if (x > 0) {
				cd.x = x - 1;
				cd.y = (SQUARE_LENGTH - 1);
				return cd;
			}
		}
		return null;
	}

	/**
	 * 根据数独规则，计算在坐标(x,y)处可选的数字列表
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public ArrayList<Integer> calcOptValueList(int x, int y) {
		return calcOptValueList(x, y, false);
	}

	/**
	 * 根据数独规则，计算在坐标(x,y)处可选的数字列表
	 * 
	 * @param x
	 * @param y
	 * @param shuffle
	 *            是否打乱顺序，打乱顺序用于生成数独矩阵；求解时不用
	 * @return
	 */
	private ArrayList<Integer> calcOptValueList(int x, int y, boolean shuffle) {
		ArrayList<Integer> al = new ArrayList<Integer>();
		// [1 ~ SQUARE_LENGTH]
		for (int i = 1; i <= SQUARE_LENGTH; i++) {
			al.add(i);
		}
		// 除去同一行已经有的数字
		for (int j = 0; j < matrix[x].length; j++) {
			al.remove(new Integer(matrix[x][j]));
		}
		// 除去同一列已经有的数字
		for (int i = 0; i < matrix.length; i++) {
			al.remove(new Integer(matrix[i][y]));
		}
		// 去除同一个3*3小矩阵中已有的数字
		int top = x / SQUARE_ROOT * SQUARE_ROOT, left = y - y % SQUARE_ROOT;
		for (int i = top; i < top + SQUARE_ROOT; i++) {
			for (int j = left; j < left + SQUARE_ROOT; j++) {
				al.remove(new Integer(matrix[i][j]));
			}
		}
		if (shuffle) {
			// 随机打乱数字列表
			Collections.shuffle(al);
		}
		return al;
	}

	/**
	 * 在控制台打印数独矩阵，用制表符表示单元格
	 * 
	 * @param matrix
	 */
	public static void printMatrix(int[][] matrix) {
		System.out.println(" ───────────────────────── ");
		for (int i = 0; i < matrix.length; i++) {
			System.out.print(" │ ");
			for (int j = 0; j < matrix[i].length; j++) {
				int v = matrix[i][j];
				System.out.print((v < 1 || v > SQUARE_LENGTH) ? " " : v);
				if (j % SQUARE_ROOT == (SQUARE_ROOT - 1)) {
					System.out.print(" │ ");
				} else {
					System.out.print("|");
				}
			}
			System.out.println();
			if (i % SQUARE_ROOT == (SQUARE_ROOT - 1)) {
				System.out.println(" ───────────────────────── ");
			}
		}
	}

	/**
	 * 求解矩阵中没有设定的单元格
	 * 
	 * @return
	 */
	public boolean solve() {
		return solve(null);
	}

	/**
	 * 求解矩阵中没有设定的单元格，每次迭代结束都之行callBack方法
	 * 
	 * @return
	 */
	public boolean solve(SolveCallback callBack) {
		// 初始化
		solveInit();

		int count = 0, circle = 0;
		boolean isAllSolved = false;// 所有已经被解决
		boolean hasAchievement = true;// 一次遍历中有所突破，即有单元格被求解了
		while (!isAllSolved) {
			circle++;
			isAllSolved = true;
			hasAchievement = false;
			// (1) 遍历所有未解决的, 如果某个单元格可选值大小为1，则表示已解决
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					Cell c = cells[i][j];
					if (!c.isPreset() && !c.isSolved()) {
						if (c.checkIsSolved()) {
							matrix[i][j] = c.getValue();
							if (callBack != null) {
								callBack.solveCellCallback(i, j, matrix[i][j]);
							}
							reduceOptValue(i, j);
							if (callBack != null) {
								callBack.reduceCellCallback(i, j, matrix[i][j]);
							}
							hasAchievement = true;
						} else {
							isAllSolved = false;
						}
					}
				}
			}
			count++;
			if (callBack != null) {
				callBack.iterateCallback(count, isAllSolved, hasAchievement);
			}
			if (DEBUG) {
				System.out.println("circle = " + circle);
				System.out.println("count = " + count);
				System.out.println("hasAchievement = " + hasAchievement);
				printMatrix(matrix);
			}
			if (isAllSolved) {
				break;
			}
			/*
			 * (2) 如果某个域(行,列,或3*3矩阵)内，未解决的单元格数字列表中，一个数字只出现在了某一个单元格中，则表示该单元格的数字确定
			 */
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					Cell c = cells[i][j];
					if (!c.isPreset() && !c.isSolved()) {
						// 遍历该单元格的可选值
						ArrayList<Integer> al = c.getOptValueList();
						int spVal = UNSET_VALUE;
						for (int p = 0; p < al.size(); p++) {
							int v = al.get(p);
							// (2.1) 遍历一行中未解决的可选特殊值
							boolean isRowUnique = judgeUniqueOptValInRow(i, j, v);
							count++;
							if (isRowUnique) {// v不在同一行中其他可选值列表里，v是一个可选特殊值，设定该单元格被解决
								spVal = v;
								if (DEBUG) {
									System.out.println("hasAchievement = (" + i + "," + j
											+ ") special value in same row");
								}
								break;// 跳出for (int p = 0; p < al.size(); p++)
							}
							// (2.2) 遍历一列中未解决的可选特殊值
							boolean isColUnique = judgeUniqueOptValInCol(i, j, v);
							count++;
							if (isColUnique) {// v不在同一列中其他可选值列表里，v是一个可选特殊值，设定该单元格被解决
								spVal = v;
								if (DEBUG) {
									System.out.println("hasAchievement = (" + i + "," + j
											+ ") special value in same column");
								}
								break;
							}
							// (2.3) 遍历一3*3矩阵中未解决的可选特殊值
							boolean isUnique3 = judgeUniqueOptValInBlock(i, j, v);
							count++;
							if (isUnique3) {// v不在同一3*3矩阵中其他可选值列表里，v是一个可选特殊值，设定该单元格被解决
								spVal = v;
								if (DEBUG) {
									System.out.println("hasAchievement = (" + i + "," + j
											+ ") special value in same block");
								}
								break;
							}
						}
						if (spVal != UNSET_VALUE) {
							c.solveByValue(spVal);
							matrix[i][j] = spVal;
							if (callBack != null) {
								callBack.solveCellCallback(i, j, spVal);
							}
							reduceOptValue(i, j);
							if (callBack != null) {
								callBack.reduceCellCallback(i, j, spVal);
							}
							hasAchievement = true;
							if (DEBUG) {
								System.out.println("circle = " + circle);
								System.out.println("count = " + count);
								printMatrix(matrix);
							}
						}
					}
				}
			}
			count++;
			if (callBack != null) {
				callBack.iterateCallback(count, isAllSolved, hasAchievement);
			}
			if (DEBUG) {
				System.out.println("circle = " + circle);
				System.out.println("count = " + count);
				System.out.println("hasAchievement = " + hasAchievement);
				printMatrix(matrix);
			}
			if (!hasAchievement) {// 遍历后什么也没查出来
				// 通过任意选值来解决
				for (int i = 0; i < matrix.length; i++) {
					for (int j = 0; j < matrix[i].length; j++) {
						Cell c = cells[i][j];
						if (!c.isPreset() && !c.isSolved()) {
							ArrayList<Integer> al = c.getOptValueList();
							if (al.size() == 0) {
								continue;
							}
							int anyV = al.get(new Random().nextInt(al.size()));
							c.solveByValue(anyV);
							matrix[i][j] = anyV;
							if (callBack != null) {
								callBack.solveCellCallback(i, j, anyV);
							}
							reduceOptValue(i, j);
							if (callBack != null) {
								callBack.reduceCellCallback(i, j, anyV);
							}
							hasAchievement = true;
							if (DEBUG) {
								System.out.println("circle = " + circle);
								System.out.println("count = " + count);
								System.out.println("hasAchievement = by random choose");
								printMatrix(matrix);
							}
							break;
						}
					}
					if (hasAchievement) {
						break;
					}
				}
				if (!hasAchievement) {
					break;// 任意选值也失败了
				}
			}
		}
		if (DEBUG) {
			if (!isAllSolved) {
				for (int i = 0; i < matrix.length; i++) {
					for (int j = 0; j < matrix[i].length; j++) {
						Cell c = cells[i][j];
						if (!c.isPreset() && !c.isSolved()) {
							System.out.println("(" + i + "," + j + ") " + c.getOptValueList());
						}
					}
				}
			}
		}
		return isAllSolved;
	}

	/**
	 * 求解初始化
	 */
	public void solveInit() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				Cell c = cells[i][j];
				if (c == null) {
					cells[i][j] = c = new Cell(matrix[i][j]);
				} else {
					c.initByValue(matrix[i][j]);
				}
				if (!c.isPreset()) {
					ArrayList<Integer> al = calcOptValueList(i, j);
					c.setOptValueList(al);
				}
			}
		}
	}

	/**
	 * 按坐标(x,y)的值，归约域(行,列,3*3矩阵)的可选值列表
	 * 
	 * @param x
	 * @param y
	 */
	private void reduceOptValue(int x, int y) {
		int v = matrix[x][y];
		// 从该行未解决的单元格中删除v
		for (int t = 0; t < matrix[x].length; t++) {
			Cell tmpC = cells[x][t];
			if (!tmpC.isPreset() && !tmpC.isSolved()) {
				tmpC.remove(v);
			}
		}
		// 从该列未解决的单元格中删除v
		for (int s = 0; s < matrix.length; s++) {
			Cell tmpC = cells[s][y];
			if (!tmpC.isPreset() && !tmpC.isSolved()) {
				tmpC.remove(v);
			}
		}
		// 从该3*3矩阵未解决的单元格中删除v
		int top = x / SQUARE_ROOT * SQUARE_ROOT, left = y - y % SQUARE_ROOT;
		for (int s = top; s < top + SQUARE_ROOT; s++) {
			for (int t = left; t < left + SQUARE_ROOT; t++) {
				Cell tmpC = cells[s][t];
				if (!tmpC.isPreset() && !tmpC.isSolved()) {
					tmpC.remove(v);
				}
			}
		}
	}

	/**
	 * 判断单元格(x,y)可选值optVal，是否在同一行所有单元格可选值列表中唯一
	 * 
	 * @param x
	 * @param y
	 * @param optVal
	 * @return
	 */
	private boolean judgeUniqueOptValInRow(int x, int y, int optVal) {
		for (int t = 0; t < matrix[x].length; t++) {
			if (t == y) {
				continue;
			}
			Cell tmpC = cells[x][t];
			if (!tmpC.isPreset() && !tmpC.isSolved()) {
				if (tmpC.getOptValueList().contains(optVal)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 判断单元格(x,y)可选值optVal，是否在同一列所有单元格可选值列表中唯一
	 * 
	 * @param x
	 * @param y
	 * @param optVal
	 * @return
	 */
	private boolean judgeUniqueOptValInCol(int x, int y, int optVal) {
		for (int s = 0; s < matrix.length; s++) {
			if (s == x) {
				continue;
			}
			Cell tmpC = cells[s][y];
			if (!tmpC.isPreset() && !tmpC.isSolved()) {
				if (tmpC.getOptValueList().contains(optVal)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 判断单元格(x,y)可选值optVal，是否在同一3*3小矩阵所有单元格可选值列表中唯一
	 * 
	 * @param x
	 * @param y
	 * @param optVal
	 * @return
	 */
	private boolean judgeUniqueOptValInBlock(int x, int y, int optVal) {
		int top = x / SQUARE_ROOT * SQUARE_ROOT, left = y - y % SQUARE_ROOT;
		for (int s = top; s < top + SQUARE_ROOT; s++) {
			for (int t = left; t < left + SQUARE_ROOT; t++) {
				if (s == x && t == y) {
					continue;
				}
				Cell tmpC = cells[s][t];
				if (!tmpC.isPreset() && !tmpC.isSolved()) {
					if (tmpC.getOptValueList().contains(optVal)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * 检测是否还有未填充的单元格
	 * 
	 * @return
	 */
	public boolean hasUnsolvedCell() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] < 1 || matrix[i][j] > SQUARE_LENGTH) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 未填充的单元格的数量
	 * 
	 * @return
	 */
	public int unsolvedCount() {
		int count = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] < 1 || matrix[i][j] > SQUARE_LENGTH) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * 判断 (x,y)坐标处的单元格是否已被解决
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isCellSolved(int x, int y) {
		return cells[x][y].isPreset() || cells[x][y].isSolved();
	}

	/**
	 * 获得 (x,y)坐标处的值
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getCellValue(int x, int y) {
		return matrix[x][y];
	}

	/**
	 * 获得 (x,y)坐标处的可选值
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public ArrayList<Integer> getCellOptValueList(int x, int y) {
		Cell c = cells[x][y];
		if (c.isPreset() || c.isSolved()) {
			return null;
		}
		if (c.isEmpty()) {
			c.setOptValueList(calcOptValueList(x, y));
		}
		return c.getOptValueList();
	}

	public Cell getCell(int x, int y) {
		return cells[x][y];
	}

	public void setCellSolved(int x, int y) {
		cells[x][y].setSolved(true);
	}

	/**
	 * 设定坐标(x,y)处的值，如果合法这设置成功，不合法则失败
	 * 
	 * @param x
	 * @param y
	 * @param value
	 * @return
	 */
	public boolean setCellValue(int x, int y, int value) {
		if (x < 0 || x > SQUARE_LENGTH || y < 0 || y > SQUARE_LENGTH) {
			return false;
		}
		matrix[x][y] = value;
		if (checkCell(x, y)) {
			return true;
		} else {
			matrix[x][y] = UNSET_VALUE;
			return false;
		}
	}

	/**
	 * 清除(x,y)坐标值
	 * 
	 * @param x
	 * @param y
	 */
	public void unsetCellValue(int x, int y) {
		matrix[x][y] = UNSET_VALUE;
	}

	/**
	 * 检测单元格(x,y)的值是否合法
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean checkCell(int x, int y) {
		int v = matrix[x][y];
		if (v < 1 || v > SQUARE_LENGTH) {
			return false;
		}
		// 检测同一行是否有重复数字
		for (int j = 0; j < matrix[x].length; j++) {
			if (y != j && v == matrix[x][j]) {
				return false;
			}
		}
		// 检测同一列是否有重复数字
		for (int i = 0; i < matrix.length; i++) {
			if (x != i && v == matrix[i][y]) {
				return false;
			}
		}
		// 检测同一个3*3小矩阵中是否有重复数字
		int top = x / SQUARE_ROOT * SQUARE_ROOT, left = y - y % SQUARE_ROOT;
		for (int i = top; i < top + SQUARE_ROOT; i++) {
			for (int j = left; j < left + SQUARE_ROOT; j++) {
				if (x != i && y != j && v == matrix[i][j]) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 若在单元格(x,y)放置给定值v, 返回与它冲突的位置
	 * 
	 * @param x
	 * @param y
	 * @param v
	 * @return
	 */
	public Coord findConflictCell(int x, int y, int v) {
		Coord cd = new Coord();
		if (v < 1 || v > SQUARE_LENGTH) {
			throw new IllegalArgumentException("超出范围的参数：" + v + ", 应在[1-9]之间!");
		}
		// 检测同一个3*3小矩阵中是否有重复数字
		int top = x / SQUARE_ROOT * SQUARE_ROOT, left = y - y % SQUARE_ROOT;
		for (int i = top; i < top + SQUARE_ROOT; i++) {
			for (int j = left; j < left + SQUARE_ROOT; j++) {
				if (x != i && y != j && v == matrix[i][j]) {
					cd.x = i;
					cd.y = j;
					return cd;
				}
			}
		}
		// 检测同一行是否有重复数字
		for (int j = 0; j < matrix[x].length; j++) {
			if (y != j && v == matrix[x][j]) {
				cd.x = x;
				cd.y = j;
				return cd;
			}
		}
		// 检测同一列是否有重复数字
		for (int i = 0; i < matrix.length; i++) {
			if (x != i && v == matrix[i][y]) {
				cd.x = i;
				cd.y = y;
				return cd;
			}
		}
		return null;
	}

	/**
	 * 检验矩阵是否是一个合法的矩阵
	 * 
	 * @return
	 */
	public boolean checkMatrix() {
		// 用数值集合检测
		HashSet<Integer> numSet = new HashSet<Integer>();
		// 横向检测
		for (int i = 0; i < SQUARE_LENGTH; i++) {
			numSet.clear();
			for (int t = 0; t < matrix[i].length; t++) {
				int v = matrix[i][t];
				if (v < 1 || v > SQUARE_LENGTH) {// 忽略待求解的
					continue;
				}
				numSet.add(v);
			}
			if (numSet.size() < SQUARE_LENGTH) {// 集合大小小于SQUARE_LENGTH
				return false;
			}
		}
		// 纵向检测
		for (int j = 0; j < SQUARE_LENGTH; j++) {
			numSet.clear();
			for (int s = 0; s < matrix.length; s++) {
				int v = matrix[s][j];
				if (v < 1 || v > SQUARE_LENGTH) {// 忽略待求解的
					continue;
				}
				numSet.add(v);
			}
			if (numSet.size() < SQUARE_LENGTH) {// 集合大小小于SQUARE_LENGTH
				return false;
			}
		}
		// 3*3小矩阵检测
		for (int i = 0; i < matrix.length; i += SQUARE_ROOT) {
			for (int j = 0; j < matrix[i].length; j += SQUARE_ROOT) {
				numSet.clear();
				// 当同一3*3矩阵中出现重复数字时，返回false
				for (int s = i; s < i + SQUARE_ROOT; s++) {
					for (int t = j; t < j + SQUARE_ROOT; t++) {
						int v = matrix[s][t];
						if (v < 1 || v > SQUARE_LENGTH) {// 忽略待求解的
							continue;
						}
						numSet.add(v);
					}
				}
				if (numSet.size() < SQUARE_LENGTH) {// 集合大小小于SQUARE_LENGTH
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 检验一个不完全矩阵是否是一个合法的矩阵，如果含有重复数字则不合法
	 * 
	 * @return
	 */
	public boolean checkUnfullMatrix() {
		// 用数值集合检测
		HashSet<Integer> numSet = new HashSet<Integer>();
		// 横向检测
		for (int i = 0; i < SQUARE_LENGTH; i++) {
			numSet.clear();
			for (int t = 0; t < matrix[i].length; t++) {
				int v = matrix[i][t];
				if (v < 1 || v > SQUARE_LENGTH) {// 忽略待求解的
					continue;
				}
				if (numSet.contains(v)) {
					return false;
				}
				numSet.add(v);
			}
		}
		// 纵向检测
		for (int j = 0; j < SQUARE_LENGTH; j++) {
			numSet.clear();
			for (int s = 0; s < matrix.length; s++) {
				int v = matrix[s][j];
				if (v < 1 || v > SQUARE_LENGTH) {// 忽略待求解的
					continue;
				}
				if (numSet.contains(v)) {
					return false;
				}
				numSet.add(v);
			}
		}
		// 3*3小矩阵检测
		for (int i = 0; i < matrix.length; i += SQUARE_ROOT) {
			for (int j = 0; j < matrix[i].length; j += SQUARE_ROOT) {
				numSet.clear();
				// 当同一3*3矩阵中出现重复数字时，返回false
				for (int s = i; s < i + SQUARE_ROOT; s++) {
					for (int t = j; t < j + SQUARE_ROOT; t++) {
						int v = matrix[s][t];
						if (v < 1 || v > SQUARE_LENGTH) {// 忽略待求解的
							continue;
						}
						if (numSet.contains(v)) {
							return false;
						}
						numSet.add(v);
					}
				}
			}
		}
		return true;
	}
}
