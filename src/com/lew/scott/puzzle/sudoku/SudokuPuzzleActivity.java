package com.lew.scott.puzzle.sudoku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.lew.scott.puzzle.sudoku.data.Cell;
import com.lew.scott.puzzle.sudoku.data.Coord;
import com.lew.scott.puzzle.sudoku.data.SolveCallback;
import com.lew.scott.puzzle.sudoku.data.SudokuMatrix;

public class SudokuPuzzleActivity extends Activity
		implements OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener,
		OnClickListener {

	private static final int DEFAULT_FIELD_COLOR = Color.WHITE;
	private static final int NO_VALUE_FIELD_COLOR = Color.GRAY;
	private static final int SELECTED_FIELD_COLOR = Color.YELLOW;
	private static final int CONFLICT_FIELD_COLOR = Color.YELLOW;
	private static final int NEW_SOLVED_FIELD_COLOR = Color.GREEN;
	private static final int USER_SOLVED_FIELD_COLOR = Color.LTGRAY;
	public static final int FAKE_BIG_GRID_BORDER_COLOR = Color.rgb(200, 100, 0);
	public static final int FAKE_GRID_BORDER_COLOR = Color.rgb(68, 68, 68);
	public static final String VIEW_OBJ_VAL_KEY = "numview";
	public static final String VIEW_OBJ_BG_KEY = "bgcolor";
	public static final String VIEW_OBJ_TRY_TIMES_KEY = "trytimes";
	public static final String NO_VALUE_TEXT = "";

	private static final int NEW_PUZZLE_DLG = 1;

	// 新游戏设定视图中的元素
	private Dialog mNewPuzzleDlg = null;
	private EditText mDegreeInput = null;
	private Button mBtnDiscard = null;
	private Button mBtnOk = null;

	private GridView matrixGridView;
	private TextView msgTextView;
	private SudokuMatrix sudokuMatrix;

	private View selectedView = null;
	private int selectedPosition = -1;
	private int currentDegree = SudokuMatrix.DEFAULT_DEGREE;
	private int hintTimes;

	private boolean isDoNewPuzzle; // 是否做初始化新游戏
	private boolean isFreeModel; // 是否是自由设定数字模式
	private boolean isSolving; // 是否正在求解过程中

	private float defaultNumTextSize = 20; //
	private boolean gotDefaultNumTextSize = false; //

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.main);
		initGrid();
		initButtons();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// 以这种方式得到数字单元格的默认字体大小
		if (hasFocus && !gotDefaultNumTextSize) {
			// 使用代码设置TextView需要转换px和sp的, getTextSize返回值是以像素(px)为单位的，而setTextSize()是以sp为单位的。
			// 现在setTextSize()可以指定单位，旧代码没有单位问题
			TextView tv = this.getNumViewAtPos(0);
			float fontScale = getResources().getDisplayMetrics().scaledDensity;
			defaultNumTextSize = tv.getTextSize() / fontScale;
			gotDefaultNumTextSize = true;
			Log.i("NumTextSize", "got defaultNumTextSize: " + defaultNumTextSize);
			Log.i("NumTextSize", "got getTextSize: " + tv.getTextSize());
//			Log.i("NumTextSize", "got getScaledTextSize: "+ tv.getScaledTextSize());
			Log.i("NumTextSize", "got DisplayMetrics: " + getResources().getDisplayMetrics());
		}
		if (hasFocus && isDoNewPuzzle) {
			doNewPuzzleSetting();
			isDoNewPuzzle = false;
			isFreeModel = false;
		}
	}

	/**
	 * 创建options菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// 充实菜单
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		// 注意：必须要调用超类的方法，否则无法实现意图回调
		return (super.onCreateOptionsMenu(menu));
	}

	// 初始化对话框
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case NEW_PUZZLE_DLG :
				return initNewPuzzleDialog();
		}
		return null;
	}

	private void initGrid() {
		matrixGridView = (GridView) findViewById(R.id.gridview);
		msgTextView = (TextView) findViewById(R.id.msgview);
		sudokuMatrix = new SudokuMatrix();

		int[][] puzzleMatrix = sudokuMatrix.generatePuzzle();
		List<Map<String, Object>> cells = new ArrayList<Map<String, Object>>();
		final int MATRIX_WIDTH = SudokuMatrix.SQUARE_LENGTH;
		final int total_length = MATRIX_WIDTH * MATRIX_WIDTH;
		for (int i = 0; i < total_length; i++) {
			Map<String, Object> cell = new HashMap<String, Object>();
			Coord cd = convPos2Coord(i);
			int v = puzzleMatrix[cd.x][cd.y];
			String str = "" + ((v == SudokuMatrix.UNSET_VALUE) ? NO_VALUE_TEXT : v);
			cell.put(VIEW_OBJ_VAL_KEY, str);
			int bgColor = (v == SudokuMatrix.UNSET_VALUE) ? NO_VALUE_FIELD_COLOR : DEFAULT_FIELD_COLOR;
			cell.put(VIEW_OBJ_BG_KEY, bgColor);
			cells.add(cell);
		}
		ListAdapter listAdapter = new SudokuGridAdapter(this,
				cells, R.layout.cell,
				new String[]{VIEW_OBJ_VAL_KEY}, new int[]{R.id.numview},
				VIEW_OBJ_BG_KEY, DEFAULT_FIELD_COLOR, NO_VALUE_FIELD_COLOR);
		matrixGridView.setAdapter(listAdapter);
		matrixGridView.setOnItemSelectedListener(this);
		matrixGridView.setOnItemClickListener(this);
		matrixGridView.setOnItemLongClickListener(this);
	}

	private void initButtons() {
		Button btn;
		btn = (Button) findViewById(R.id.button_1);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_2);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_3);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_4);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_5);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_6);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_7);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_8);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_9);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_unset);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_hint);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_solve);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_newpuzzle);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.button_freemodel);
		btn.setOnClickListener(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		doSelectItem(parent, view, position, id);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		doSelectItem(parent, view, position, id);
	}

	private void doSelectItem(AdapterView<?> parent, View view, int position, long id) {
		if (selectedView == view) {
			return;
		}
		hintTimes = 0;

		// 恢复上个选中cell的背景
		if (selectedView != null) {
			// Map<String, Object> itemObj = (Map<String, Object>) matrixGridView.getItemAtPosition(selectedPosition);
			// int oldcolor = (Integer) itemObj.get(VIEW_OBJ_BG_KEY);
			// selectedView.setBackgroundColor(oldcolor);
			selectedView.setBackgroundColor(FAKE_GRID_BORDER_COLOR);
		}
		// 设置选中背景颜色
		selectedPosition = position;
		selectedView = view.findViewById(R.id.borderview);
		// selectedView = (TextView) view.findViewById(R.id.numview);
		selectedView.setBackgroundColor(SELECTED_FIELD_COLOR);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		// 显示提示信息
		doHint(position);
		return false;
	}

	private void doHint(int position) {
		if (position == -1) {
			msgTextView.setText("请选择一个单元格");
			return;
		}
		Coord cd = convPos2Coord(position);
		if (sudokuMatrix.isCellSolved(cd.x, cd.y)) {
			msgTextView.setText("请选择另一个单元格");
			return;
		}
		if (hintTimes == 0) {
			hintCell(position);
		} else if (hintTimes == 1) {
			hintBlock(position);
		} else if (hintTimes == 2) {
			hintRow(position);
			hintColumn(position);
		} else {
			msgTextView.setText("贪得无厌的家伙！。");
			return;
		}
		msgTextView.setText("提示信息");
		hintTimes++;
	}

	// 把位置转换为坐标
	public static Coord convPos2Coord(int position) {
		Coord cd = new Coord();
		cd.x = position / SudokuMatrix.SQUARE_LENGTH;
		cd.y = position % SudokuMatrix.SQUARE_LENGTH;
		return cd;
	}

	// 把坐标转换为位置
	public static int convCoord2Pos(Coord cd) {
		return cd.x * SudokuMatrix.SQUARE_LENGTH + cd.y;
	}

	public static int convCoord2Pos(int x, int y) {
		return x * SudokuMatrix.SQUARE_LENGTH + y;
	}

	@Override
	public void onClick(View v) {
		if (isSolving) {// 正在求解过程中，不准打断
			return;
		}
		int btnId = v.getId();
		if (btnId == R.id.button_unset) {
			doUnset();
		} else if (btnId == R.id.button_hint) {
			doHint(selectedPosition);
		} else if (btnId == R.id.button_solve) {
			doSolve();
		} else if (btnId == R.id.button_newpuzzle) {
			doNewPuzzle();
		} else if (btnId == R.id.button_freemodel) {
			doCustomizeMatrix();
		}
		Button btn = (Button) v;
		String cmdStr = btn.getText().toString();
		// 数字命令
		if (cmdStr.charAt(0) >= '1' && cmdStr.charAt(0) <= '9') {
			doSetNum(cmdStr);
			return;
		}
	}

	/**
	 * options菜单相应函数
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.about : {
				doAbout();
				break;
			}
			case R.id.exit : {
				this.finish();
				break;
			}
		}

		return (super.onOptionsItemSelected(item));
	}

	/**
	 * 设定数字命令
	 * 
	 * @param cmdStr
	 */
	private void doSetNum(String cmdStr) {
		if (selectedView == null || selectedPosition == -1) {
			msgTextView.setText("请选择一个单元格");
			return;
		}
		Coord cd = convPos2Coord(selectedPosition);
		if (sudokuMatrix.isCellSolved(cd.x, cd.y)) {
			msgTextView.setText("请选择另一个单元格");
			return;
		}

		// 尝试次数增加1
		int tryTimes = increaseTryTimes(selectedPosition, 1);

		int cmdNum = cmdStr.charAt(0) - '0';
		// 判断数字冲突
		Coord cccd = sudokuMatrix.findConflictCell(cd.x, cd.y, cmdNum);
		if (cccd != null) {
			msgTextView.setText("Oops!!");
			int ccPos = convCoord2Pos(cccd);
			final View ccView = matrixGridView.getChildAt(ccPos).findViewById(R.id.borderview);
			Animation anim = new AlphaAnimation(1, 0);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					ccView.setBackgroundColor(CONFLICT_FIELD_COLOR);
				}
				public void onAnimationRepeat(Animation animation) {
				}
				public void onAnimationEnd(Animation animation) {
					ccView.setBackgroundColor(FAKE_GRID_BORDER_COLOR);
				}
			});
			anim.setDuration(100);
			anim.setRepeatCount(1);
			ccView.startAnimation(anim);
			return;
		}
		// 设定该单元格数字
		// selectedView.setText(cmdStr);
		TextView curTextView = (TextView) selectedView.findViewById(R.id.numview);
		curTextView.setTextSize(defaultNumTextSize);
		curTextView.setText(cmdStr);
		curTextView.setBackgroundColor(USER_SOLVED_FIELD_COLOR);
		// 同时设置 matrixGridView item 值
		Map<String, Object> itemObj = (Map<String, Object>) matrixGridView.getItemAtPosition(selectedPosition);
		itemObj.put(VIEW_OBJ_VAL_KEY, cmdStr);
		itemObj.put(VIEW_OBJ_BG_KEY, USER_SOLVED_FIELD_COLOR);
		sudokuMatrix.setCellValue(cd.x, cd.y, cmdNum);
		if (isFreeModel) {
			return;
		}
		int[][] answer = sudokuMatrix.getAnswerMatrix();
		// sudokuMatrix.setCellSolved(cd.x, cd.y);
		if (cmdNum == answer[cd.x][cd.y] && tryTimes == 1) {
			msgTextView.setText("你真聪明, 一次就填对了。");
			// msgTextView.setText("嗯，对了。");
		} else {
			msgTextView.setText("请继续。");
		}
		if (!sudokuMatrix.hasUnsolvedCell()) {
			msgTextView.setText("你真聪明,全部解决了！");
		}
	}

	private void doUnset() {
		if (selectedView == null || selectedPosition == -1) {
			msgTextView.setText("请选择一个单元格");
			return;
		}
		Coord cd = convPos2Coord(selectedPosition);
		Cell cell = sudokuMatrix.getCell(cd.x, cd.y);
		if (cell.isPreset()) {
			msgTextView.setText("请选择另一个单元格");
			return;
		}

		sudokuMatrix.unsetCellValue(cd.x, cd.y);
		cell.unset();
		TextView curTextView = getNumViewAtPos(selectedPosition);
		curTextView.setText(NO_VALUE_TEXT);
		curTextView.setBackgroundColor(NO_VALUE_FIELD_COLOR);
		// 同时清空 matrixGridView item 值
		Map<String, Object> itemObj = (Map<String, Object>) matrixGridView.getItemAtPosition(selectedPosition);
		itemObj.put(VIEW_OBJ_VAL_KEY, NO_VALUE_TEXT);
		itemObj.put(VIEW_OBJ_BG_KEY, NO_VALUE_FIELD_COLOR);
	}

	private void doSolve() {
		isSolving = true;
		// 把输入框矩阵数字，保存到this.sudokuMatrix
		initByHandFromView2Matrix();
		if (sudokuMatrix.unsolvedCount() > 69) {
			msgTextView.setText("你一定在开玩笑，程序拒绝求解。");
			isSolving = false;
			return;
		}
		final int sleep = 500;
		if (sudokuMatrix.unsolvedCount() > 50) {
			msgTextView.setText("你在开玩笑吧，空白处太多了，可能求解失败。");
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {
			}
		}
		// 开始求解
		msgTextView.setText("正在求解...");
		new Thread(new Runnable() {
			public void run() {
				boolean flag = sudokuMatrix.solve(new SolveCallback() {
					public void iterateCallback(final int itrTimes, boolean isAllSolved, boolean hasAchievement) {
						msgTextView.post(new Runnable() {
							public void run() {
								msgTextView.setText("迭代" + itrTimes + "次");
							}
						});
					}

					public void solveCellCallback(int x, int y, final int value) {
						final int pos = convCoord2Pos(x, y);
						final TextView tv = getNumViewAtPos(pos);
						tv.post(new Runnable() {
							public void run() {
								tv.setTextSize(defaultNumTextSize);
								tv.setText("" + value);
								tv.setBackgroundColor(NEW_SOLVED_FIELD_COLOR);
								// 同时设置 matrixGridView item 值
								Map<String, Object> itemObj = (Map<String, Object>) matrixGridView.getItemAtPosition(pos);
								itemObj.put(VIEW_OBJ_VAL_KEY, "" + value);
								itemObj.put(VIEW_OBJ_BG_KEY, NEW_SOLVED_FIELD_COLOR);
							}
						});
						try {
							Thread.sleep(sleep);
						} catch (Exception e) {
						}
					}

					public void reduceCellCallback(int x, int y, int value) {

					}

				});
				final String msg = flag ? "求解成功。" : "求解失败。";
				msgTextView.post(new Runnable() {
					public void run() {
						msgTextView.setText(msg);
					}
				});
				isSolving = false;
			}
		}).start();

	}

	/**
	 * 按文本框输入的数字，初始化数独矩阵对象
	 */
	private void initByHandFromView2Matrix() {
		for (int i = 0; i < SudokuMatrix.SQUARE_LENGTH; i++) {
			for (int j = 0; j < SudokuMatrix.SQUARE_LENGTH; j++) {
				int pos = convCoord2Pos(i, j);
				TextView tv = getNumViewAtPos(pos);
				try {
					int v = Integer.parseInt(tv.getText().toString());
					if (sudokuMatrix.setCellValue(i, j, v)) {// 尝试给(i,j)设定坐标值
						tv.setBackgroundColor(DEFAULT_FIELD_COLOR);
						continue;
					}
				} catch (Exception ex) {
				}
				// 设定单元格(i,j)值失败
				tv.setText(NO_VALUE_TEXT);
				tv.setBackgroundColor(NO_VALUE_FIELD_COLOR);
				sudokuMatrix.unsetCellValue(i, j);
			}
		}
	}

	private void doNewPuzzle() {
		showDialog(NEW_PUZZLE_DLG);
	}

	private Dialog initNewPuzzleDialog() {
		// 初始化新谜题设置难度级别数对话框
		mNewPuzzleDlg = new Dialog(this);
		// LayoutInflater inflater = this.getLayoutInflater();
		LayoutInflater inflater = mNewPuzzleDlg.getLayoutInflater();
		View newGameSettingView = inflater.inflate(R.layout.new_game_view, null);
		// 必须先填充，再获取其子组件
		mNewPuzzleDlg.setContentView(newGameSettingView);
		mNewPuzzleDlg.setTitle("请输入难度级别数[" + SudokuMatrix.MIN_DEGREE + "," + SudokuMatrix.MAX_DEGREE
				+ "]：");

		mDegreeInput = (EditText) newGameSettingView.findViewById(R.id.degree_input);
		mBtnDiscard = (Button) newGameSettingView.findViewById(R.id.BTN_DISCARD);
		mBtnOk = (Button) newGameSettingView.findViewById(R.id.BTN_OK);

		OnClickListener cl = new OnClickListener() {
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.BTN_DISCARD : {
						mNewPuzzleDlg.dismiss();
						break;
					}
					case R.id.BTN_OK : {
						// 关闭输入法软键盘，否则初始化GridView会出错
						InputMethodManager imm = (InputMethodManager) mDegreeInput.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(mDegreeInput.getWindowToken(), 0);
						try {
							Thread.sleep(200);
						} catch (Exception e) {
						}
						mNewPuzzleDlg.dismiss();
						isDoNewPuzzle = true;
						break;
					}
				}
			}
		};
		mBtnDiscard.setOnClickListener(cl);
		mBtnOk.setOnClickListener(cl);
		return mNewPuzzleDlg;
	}

	private void doNewPuzzleSetting() {
		int degree = 0;
		try {
			degree = Integer.parseInt(mDegreeInput.getText().toString());
		} catch (Exception ex) {
		}
		if ((degree < SudokuMatrix.MIN_DEGREE) || (degree > SudokuMatrix.MAX_DEGREE)) {
			degree = this.currentDegree;
		}
		msgTextView.setText("新游戏，难度级别为" + degree);
		// 生成谜题
		this.currentDegree = degree;
		sudokuMatrix.setDegreeOfPuzzle(degree);
		int[][] puzzleMatrix = sudokuMatrix.generatePuzzle();

		final int MATRIX_WIDTH = SudokuMatrix.SQUARE_LENGTH;
		final int total_length = MATRIX_WIDTH * MATRIX_WIDTH;
		for (int i = 0; i < total_length; i++) {
			Map<String, Object> cell = (Map<String, Object>) matrixGridView.getItemAtPosition(i);
			TextView tv = getNumViewAtPos(i);
			Coord cd = convPos2Coord(i);
			int v = puzzleMatrix[cd.x][cd.y];
			String str = "" + ((v == SudokuMatrix.UNSET_VALUE) ? NO_VALUE_TEXT : v);
			int bgColor = (v == SudokuMatrix.UNSET_VALUE) ? NO_VALUE_FIELD_COLOR : DEFAULT_FIELD_COLOR;
			cell.put(VIEW_OBJ_VAL_KEY, str);
			cell.put(VIEW_OBJ_BG_KEY, bgColor);
			cell.put(VIEW_OBJ_TRY_TIMES_KEY, 0);
			tv.setTextSize(defaultNumTextSize);
			tv.setText(str);
			tv.setBackgroundColor(bgColor);
		}
		hintTimes = 0;
	}

	private void doAbout() {
		final Dialog aboutDlg = new Dialog(this);
		LayoutInflater inflater = aboutDlg.getLayoutInflater();
		View aboutView = inflater.inflate(R.layout.about_view, null);
		aboutDlg.setContentView(aboutView);
		aboutDlg.setTitle("关于数独之谜");

		aboutView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				aboutDlg.dismiss();
			}
		});
		aboutDlg.show();
	}

	private void doCustomizeMatrix() {
		for (int i = 0; i < SudokuMatrix.SQUARE_LENGTH; i++) {
			for (int j = 0; j < SudokuMatrix.SQUARE_LENGTH; j++) {
				int pos = convCoord2Pos(i, j);
				TextView tv = getNumViewAtPos(pos);
				// 清空单元格(i,j)值
				tv.setText(NO_VALUE_TEXT);
				tv.setBackgroundColor(NO_VALUE_FIELD_COLOR);
				// 同时清空 matrixGridView item 值
				Map<String, Object> itemObj = (Map<String, Object>) matrixGridView.getItemAtPosition(pos);
				itemObj.put(VIEW_OBJ_VAL_KEY, NO_VALUE_TEXT);
				itemObj.put(VIEW_OBJ_BG_KEY, NO_VALUE_FIELD_COLOR);
				sudokuMatrix.unsetCellValue(i, j);
				sudokuMatrix.getCell(i, j).unset();
			}
		}
		isFreeModel = true;
		msgTextView.setText("自由设定单元格内的数字，可用程序求解。");
	}

	// 得到position位置的数字视图
	private TextView getNumViewAtPos(int position) {
		View chView = matrixGridView.getChildAt(position);
		return (TextView) chView.findViewById(R.id.numview);
	}

	// 增加尝试次数
	private int increaseTryTimes(int position, int count) {
		Map<String, Object> itemObj = (Map<String, Object>) matrixGridView.getItemAtPosition(position);
		Object tryTimes = itemObj.get(VIEW_OBJ_TRY_TIMES_KEY);
		if (tryTimes == null) {
			tryTimes = new Integer(count);
		} else {
			tryTimes = Integer.valueOf(((Integer) tryTimes).intValue() + count);
		}
		itemObj.put(VIEW_OBJ_TRY_TIMES_KEY, tryTimes);
		return (Integer) tryTimes;
	}

	// 显示一个单元格的提示信息
	private boolean hintCell(int position) {
		Coord cd = convPos2Coord(position);
		if (sudokuMatrix.isCellSolved(cd.x, cd.y)) {
			msgTextView.setText("请选择另一个单元格");
			return false;
		}
		// 获得可选数字
		ArrayList<Integer> al = sudokuMatrix.getCellOptValueList(cd.x, cd.y);
		if (al == null) {
			return false;
		}
		// 设置
		setHintNums(getNumViewAtPos(position), al, position);
		// 尝试次数增加2
		increaseTryTimes(position, 2);
		return true;
	}

	// 显示一个3*3区域块的提示信息
	private boolean hintBlock(int position) {
		Coord cd = convPos2Coord(position);
		if (sudokuMatrix.isCellSolved(cd.x, cd.y)) {
			msgTextView.setText("请选择另一个单元格");
			return false;
		}
		boolean hinted = false;
		int top = cd.x / SudokuMatrix.SQUARE_ROOT * SudokuMatrix.SQUARE_ROOT, left = cd.y - cd.y
				% SudokuMatrix.SQUARE_ROOT;
		for (int s = top; s < top + SudokuMatrix.SQUARE_ROOT; s++) {
			for (int t = left; t < left + SudokuMatrix.SQUARE_ROOT; t++) {
				ArrayList<Integer> al = sudokuMatrix.getCellOptValueList(s, t);
				if (al == null) {
					continue;
				}
				int tmpPos = convCoord2Pos(s, t);
				setHintNums(getNumViewAtPos(tmpPos), al, tmpPos);
				// 尝试次数增加2
				increaseTryTimes(tmpPos, 2);
				hinted = true;
			}
		}
		return hinted;
	}

	// 显示一行的提示信息
	private boolean hintRow(int position) {
		Coord cd = convPos2Coord(position);
		if (sudokuMatrix.isCellSolved(cd.x, cd.y)) {
			msgTextView.setText("请选择另一个单元格");
			return false;
		}
		boolean hinted = false;
		for (int t = 0; t < SudokuMatrix.SQUARE_LENGTH; t++) {
			ArrayList<Integer> al = sudokuMatrix.getCellOptValueList(cd.x, t);
			if (al == null) {
				continue;
			}
			int tmpPos = convCoord2Pos(cd.x, t);
			setHintNums(getNumViewAtPos(tmpPos), al, tmpPos);
			// 尝试次数增加2
			increaseTryTimes(tmpPos, 2);
			hinted = true;
		}
		return hinted;
	}

	// 显示一列的提示信息
	private boolean hintColumn(int position) {
		Coord cd = convPos2Coord(position);
		if (sudokuMatrix.isCellSolved(cd.x, cd.y)) {
			msgTextView.setText("请选择另一个单元格");
			return false;
		}
		boolean hinted = false;
		for (int s = 0; s < SudokuMatrix.SQUARE_LENGTH; s++) {
			ArrayList<Integer> al = sudokuMatrix.getCellOptValueList(s, cd.y);
			if (al == null) {
				continue;
			}
			int tmpPos = convCoord2Pos(s, cd.y);
			setHintNums(getNumViewAtPos(tmpPos), al, tmpPos);
			// 尝试次数增加2
			increaseTryTimes(tmpPos, 2);
			hinted = true;
		}
		return hinted;
	}

	private void setHintNums(TextView tv, ArrayList<Integer> al, int pos) {
		if (tv.getText().length()>0) {
			return;
		}
		tv.setTextSize(defaultNumTextSize / 2);
		StringBuffer hint = new StringBuffer();
		for (int i = 0; i < al.size(); i++) {
			hint.append(al.get(i)).append(' ');
		}
		tv.setText(hint);
		// 同时设置 matrixGridView item 值
		Map<String, Object> itemObj = (Map<String, Object>) matrixGridView.getItemAtPosition(pos);
		itemObj.put(VIEW_OBJ_VAL_KEY, hint);
	}
}