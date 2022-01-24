// kanji=漢字
/*
 * $Id: HomeRoom.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/25 9:55:50 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nao_package.db.DB2UDB;

/**
 * 年組。
 * @version $Id: HomeRoom.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class HomeRoom implements Comparable {
    /** 学年 */
    private final String _grade;
    /** HRクラス */
    private final String _hrClass;
    /** HR名称 */
    private final String _name;
    /** クラスの学籍番号リスト */
    private final List _studentList;

    /**
     * コンストラクタ
     * @param grade 学年
     * @param hrClass HRクラス
     * @param name 略称
     */
    public HomeRoom(final String grade, final String hrClass, final String name) {
        _grade = grade;
        _hrClass = hrClass;
        _name = name;
        _studentList = new ArrayList();
    }

    /**
     * 学年を得る
     * @return 学年
     */
    public String getGrade() {
        return _grade;
    }

    /**
     * HRクラスを得る
     * @return HRクラス
     */
    public String getHrClass() {
        return _hrClass;
    }

    /**
     * HR略称を得る
     * @return HR略称
     */
    public String getName() {
        return _name;
    }

    /**
     * 学年コード+組コードを得る。
     * @return 学年コード+組コード
     */
    public String getCode() {
        return _grade + _hrClass;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _grade + ", " + _hrClass + ", " + _name;
    }

    /**
     * この年組に学生を追加する
     * @param schregno 学生の学籍番号
     */
    public void addStudent(final String schregno) {
        _studentList.add(schregno);
    }

    /**
     * この年組に指定された学生がいるか
     * @param schregno 学生の学籍番号
     * @return この年組に指定された学生がいるならtrue、そうでなければfalse
     */
    public boolean hasStudent(final String schregno) {
        return _studentList.contains(schregno);
    }

    /**
     * この年組に所属する学生の数を返す
     * @return この年組に所属する学生の数
     */
    public int getStudentCount() {
        return _studentList.size();
    }


    /**
     * この年組の在籍番号のリストを返す
     * @return 在籍番号のリスト
     */
    public List getStudentList() {
        return _studentList;
    }

    /**
     * HRクラスデータをロードする
     * @param db DB
     * @param year 年
     * @param semester 学期
     * @return HRクラスデータ
     * @throws SQLException SQL例外
     */
    public static Map load(final DB2UDB db, final String year, final String semester) throws SQLException {
        final Map rtn = new TreeMap();

        ResultSet rs = null;
        db.query(getRegdHdatSql(year, semester));
        rs = db.getResultSet();
        while (rs.next()) {
            final String grade = rs.getString("grade");
            final String hrClass = rs.getString("hr_class");
            final String name = rs.getString("hr_nameabbv");

            final HomeRoom hr = new HomeRoom(grade, hrClass, name);
            rtn.put(grade + hrClass, hr);
        }

        db.query(getRegdDatSql(year, semester));
        rs = db.getResultSet();
        while (rs.next()) {
            final String grade = rs.getString("grade");
            final String hrClass = rs.getString("hr_class");
            final String schregno = rs.getString("schregno");

            final HomeRoom hr = (HomeRoom) rtn.get(grade + hrClass);
            if (hr != null) {
                hr.addStudent(schregno);
            }
        }

        return rtn;
    }

    /**
     * 年組クラスを取得するSQLを得る
     * 学生が1人以上所属している年組を対象とする
     * @param year 学年
     * @param semester 学期
     * @return 年組を取得するSQL
     */
    private static String getRegdHdatSql(final String year, final String semester) {
        final String sql;
        sql = "SELECT"
            + "  t1.grade,"
            + "  t1.hr_class,"
            + "  t1.hr_nameabbv,"
            + "  count(*)"
            + " FROM"
            + "  schreg_regd_hdat t1"
            + "  INNER JOIN schreg_regd_dat t2 on"
            + "    t2.grade = t1.grade AND"
            + "    t2.hr_class = t1.hr_class AND"
            + "    t2.year = t1.year AND"
            + "    t2.semester = t1.semester"
            + " WHERE"
            + "  t1.year='" + year + "' AND"
            + "  t1.semester='" + semester + "' "
            + " GROUP BY"
            + "  t1.grade, t1.hr_class, t1.hr_nameabbv"
            + " HAVING"
            + "  count(*) <> 0"
            ;
        return sql;
    }

    /**
     * 年組に所属する学生を取得するSQLを得る
     * @param year 年度 
     * @param semester 学期
     * @return 年組に所属する学生を取得するSQL
     */
    private static String getRegdDatSql(final String year, final String semester) {
        final String sql;
        sql = "SELECT"
            + "  grade,"
            + "  hr_class,"
            + "  schregno"
            + " FROM"
            + "  schreg_regd_dat"
            + " WHERE"
            + "  year='" + year + "' AND"
            + "  semester='" + semester + "'"
            + " ORDER BY"
            + "  grade, hr_class, attendno"
            ;
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Object o) {
        if (!(o instanceof HomeRoom)) {
            return -1;
        }
        final HomeRoom that = (HomeRoom) o;
        int cmp = _grade.compareTo(that._grade);
        if (cmp == 0) {
            cmp = _hrClass.compareTo(that._hrClass);
        }
        return cmp;
    }
} // HomeRoom

// eof
