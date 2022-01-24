package nao_package;
/**
 * タイトル:     カレンダー<p>
 */

import java.util.*;
import javax.swing.table.*;

public class CalendarModel extends AbstractTableModel {
  private Calendar calendar;
  private int year;
  private int month;

  // 「日」を返す
/************
  private int getDate(int row, int col) {
    calendar.set(Calendar.YEAR, this.year); // 年の設定
    calendar.set(Calendar.MONTH, this.month); // 月の設定
    calendar.set(Calendar.WEEK_OF_MONTH, row + 1);
    calendar.set(Calendar.DAY_OF_WEEK, col + 1);
    return calendar.get(Calendar.DATE);
  }
*************/

  // コンストラクタ
  public CalendarModel() {
    calendar = Calendar.getInstance(); // 現在の日時に設定されたカレンダーを取得
    this.year = calendar.get(Calendar.YEAR); // 年の取得
    this.month = calendar.get(Calendar.MONTH); // 月の取得
  }
  public CalendarModel(Date ddd) {
    this(); // カレンダーインスタンスを生成
    calendar.setTime(ddd);  // 日付をカレンダー型にセット
    this.year = calendar.get(Calendar.YEAR); // 年の取得
    this.month = calendar.get(Calendar.MONTH); // 月の取得
  }

  // 日付型のオブジェクトを返す
  public Object getValueAt(int parm1, int parm2) {
/************
    int date = this.getDate(parm1, parm2);
    return new Integer(date);
************/
    calendar.set(Calendar.YEAR, this.year); // 年の設定
    calendar.set(Calendar.MONTH, this.month); // 月の設定
    calendar.set(Calendar.WEEK_OF_MONTH, parm1 + 1); // 何週目か？
    calendar.set(Calendar.DAY_OF_WEEK, parm2 + 1);   // 曜日

    Date aaa = new Date();
    aaa = calendar.getTime();
    return( aaa );
  }

  public int getColumnCount() {
    return 7;
  }

  public int getRowCount() {
    return 5;
  }

  /** 列名の設定 */
  public String getColumnName(int columnIndex) {
    String[] dayOfTheWeek = {
      "<html><font color=red>日</font>", "月", "火", "水", "木", "金", "<html><font color=blue>土</font>"
    };
    return dayOfTheWeek[columnIndex];
  }

  public void addMonth(int delta) {
    month += delta;
    if (month > Calendar.DECEMBER) { // 次の年に繰り上げ
      year++;
      month -= 12;
    }
    else if (month < Calendar.JANUARY) { // 前の年に繰り下げ
      year--;
      month += 12;
    }
  }

  // 年を返す
  public int getYear() {
    return this.year;
  }

  // 月を返す
  public int getMonth() {
    /* 注；this.monthの値はそのまま月の値ではありません！ */
    switch (this.month) {
    case Calendar.JANUARY: return 1;
    case Calendar.FEBRUARY: return 2;
    case Calendar.MARCH: return 3;
    case Calendar.APRIL: return 4;
    case Calendar.MAY: return 5;
    case Calendar.JUNE: return 6;
    case Calendar.JULY: return 7;
    case Calendar.AUGUST: return 8;
    case Calendar.SEPTEMBER: return 9;
    case Calendar.OCTOBER: return 10;
    case Calendar.NOVEMBER: return 11;
    case Calendar.DECEMBER: return 12;
    default: return 0;
    }
  }

  // 日付から日時を除いた形式にするデフォルトレンダラ
  public Class getColumnClass(int column) {
    return java.util.Date.class;
  }
}
