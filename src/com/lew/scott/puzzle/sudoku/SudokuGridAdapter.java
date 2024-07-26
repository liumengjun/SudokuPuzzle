package com.lew.scott.puzzle.sudoku;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.lew.scott.puzzle.sudoku.data.Coord;
import com.lew.scott.puzzle.sudoku.data.SudokuMatrix;

public class SudokuGridAdapter extends SimpleAdapter {

	private List<Map<String, Object>> mData;

	public SudokuGridAdapter(Context context, List<Map<String, Object>> data,
			int resource, String[] from, int[] to,
			String keyOfBgColor, int defaultBgColor, int noValueBgColor) {
		super(context, data, resource, from, to);
		this.mData = data;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		// 调整边框
		Coord cd = SudokuPuzzleActivity.convPos2Coord(position);
		int x = cd.x, y = cd.y;
		int thick = 3, thin = 0, bigGridLast = SudokuMatrix.SQUARE_ROOT - 1, last = SudokuMatrix.SQUARE_LENGTH - 1;
		int top = x == 0 ? 2 * thick : ((x % SudokuMatrix.SQUARE_ROOT == 0) ? thick : thin);
		int left = y == 0 ? 2 * thick : ((y % SudokuMatrix.SQUARE_ROOT == 0) ? thick : thin);
		int bottom = x == last ? 2 * thick : ((x % SudokuMatrix.SQUARE_ROOT == bigGridLast) ? thick : thin);
		int right = y == last ? 2 * thick : ((y % SudokuMatrix.SQUARE_ROOT == bigGridLast) ? thick : thin);
		v.setPadding(left, top, right, bottom);
		v.setBackgroundColor(SudokuPuzzleActivity.FAKE_BIG_GRID_BORDER_COLOR);
		v.findViewById(R.id.borderview).setBackgroundColor(SudokuPuzzleActivity.FAKE_GRID_BORDER_COLOR);
		// 设置背景色
		Map<String, Object> item = (Map<String, Object>) mData.get(position);
		Object bgColor = item.get(SudokuPuzzleActivity.VIEW_OBJ_BG_KEY);
		if (bgColor != null) {
			TextView tv = (TextView) v.findViewById(R.id.numview);
			tv.setBackgroundColor((Integer) bgColor);
			return v;
		}
		return v;
	}
}
