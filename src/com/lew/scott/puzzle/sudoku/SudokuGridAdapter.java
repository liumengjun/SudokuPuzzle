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
		int thick = 2, thin = 0;
		int top = (x % SudokuMatrix.SQUARE_ROOT == 0) ? thick : thin;
		int left = (y % SudokuMatrix.SQUARE_ROOT == 0) ? thick : thin;
		int bottom = (x % SudokuMatrix.SQUARE_ROOT == (SudokuMatrix.SQUARE_ROOT - 1)) ? thick : thin;
		int right = (y % SudokuMatrix.SQUARE_ROOT == (SudokuMatrix.SQUARE_ROOT - 1)) ? thick : thin;
		v.setPadding(left, top, right, bottom);
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
