// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2004/06/09 15:22:29 - JST
 * 作成者: teruya
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.hiro.knjb033;

import javax.swing.table.*;
import java.util.*;
import java.sql.*;
import nao_package.db.*;

/**
 * タイトル:  時間割・履修管理システム
 * 説明:
 * 著作権:   Copyright (c) 2004
 * 会社名:
 * @author m.miyagi
 * @version 1.0
 */

public class KNJB033_Model_2 extends DefaultTableModel {
    DB2UDB      db2;        // Databaseクラスを継承したクラス
    ResultSet   SqlResult;  // ＤＢの検索結果
    Vector[]    col_value;  // 列データ
    int[]       VecCnt;     // 列生徒数
    int         AllColCnt;  // 列数
    int         AllRowCnt;  // 行数
    String      Year;       // 選択年度
    String      Semester;   // 選択学期
    String      ChrCD;      // 選択講座コード
    String      GrpCD;      // 選択群コード
    String      StaffCD;    // 操作職員コード
    String      stclass;    // 対象クラス
    String[]    Header;     // ヘッダ表示用

    public KNJB033_Model_2()
    {
        super();
    }

    /**
     * セル編集設定
     */
    public boolean isCellEditable(int rowIndex,int colmunIndex)
    {
        /** 編集不可 */
        return false;
    }

    public int getColumnCount()
    {
        /**@todo: この javax.swing.table.AbstractTableModel abstract メソッドを実装*/
        return AllColCnt;
    }

    public Object getValueAt(int parm1, int parm2) {
        /**@todo: この javax.swing.table.AbstractTableModel abstract メソッドを実装*/
        String date = this.getData(parm1, parm2);
        return date;
    }

    public int getRowCount()
    {
        /**@todo: この javax.swing.table.AbstractTableModel abstract メソッドを実装*/

        // 川田追加項目11/29
        return AllRowCnt + 1;
    }

////////////////////////////////////////////////////////////////////

    /** クラス名簿データセット */
    public void query(DB2UDB db2)
    {
        // 川田追加項目 11/29
        String strgrade,strclass,strschregno,strlname,strfname;

        // 初期値クリア
        String sqlGrade = "";
        String sqlClass = "";
        String sql = "";

        // SQLの生成
// tajimaAdd 12/11
        for (int rpCnt = 0; rpCnt < AllColCnt; rpCnt++)
//        for (int rpCnt = 0; rpCnt < (AllColCnt + 1); rpCnt++)
// tajimaAddEnd 12/11
        {
            try
            {
                /** 学年情報設定 */
                sqlGrade = Header[rpCnt].substring(0, 2);
                /** 組情報設定 */
                sqlClass = Header[rpCnt].substring(3, 5);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                break;
            }
System.out.println("学年 = " + sqlGrade);
System.out.println("組 = " + sqlClass);
            sql = "select distinct " 
                + "T3.SCHREGNO, T3.GRADE, "
                + "T3.HR_CLASS, T3.ATTENDNO, "
                + "T4.NAME_SHOW "
                + "from "
                + "schreg_regd_dat T3, schreg_base_mst T4 "
                + "where "
                + "T3.YEAR = '" + Year + "' "
                + "and T3.SCHREGNO = T4.SCHREGNO "
                + "and T3.GRADE='" + sqlGrade + "' "
                + "and T3.HR_CLASS='" + sqlClass + "' "
                + "and T3.SEMESTER = '" + Semester + "' "
                + "and T3.ATTENDNO not in ( "
                + "select distinct "
                + "T3.ATTENDNO "
                + "from "
                + "chair_dat T1, chair_std_dat T2, "
                + "schreg_regd_dat T3, schreg_base_mst T4 "
                + "where "
                + "T1.YEAR = '" + Year + "' ";
            if(GrpCD.equals("0000")) {
            sql += "and T1.CHAIRCD='" + ChrCD + "' "
                + "and T1.GROUPCD='" + GrpCD + "' ";
            }
            else {
            sql += "and T1.GROUPCD='" + GrpCD + "' ";
            }
            sql += "and T2.YEAR='" + Year + "' "
                + "and T1.CHAIRCD = T2.CHAIRCD "
                + "and T3.YEAR = '" + Year + "' "
                + "and T2.SCHREGNO = T3.SCHREGNO "
                + "and T2.SCHREGNO = T4.SCHREGNO "
                + "and T3.GRADE='" + sqlGrade + "' "
                + "and T3.HR_CLASS='" + sqlClass + "' "
                + "and T1.SEMESTER='" + Semester + "' "
                + "and T1.SEMESTER=T2.SEMESTER "
                + "and T1.SEMESTER=T3.SEMESTER "
                + "and ((select CTRL_DATE from control_mst where CTRL_NO = '01') between T2.APPDATE and T2.APPENDDATE or T2.APPDATE is null)"
                + ") "
                + "order by T3.GRADE, "
                + "T3.HR_CLASS, T3.ATTENDNO";
                System.out.println("model2 SQL is " + sql);
            try
            {
                /** SQL文の実行 */
                db2.query(sql);
                /** 実行結果取得 */
                SqlResult = db2.getResultSet();
                String StrAttend;
                String StrGrade;
                String StrClass;
                String StrAttendNo;
                String StrName;
                String StrSchregno;
                while( SqlResult.next() )
                {
                    /** DEBUG */
                    System.out.print(SqlResult.getString("GRADE") + "-");
                    System.out.print(SqlResult.getString("HR_CLASS") + "  ");
                    System.out.print(SqlResult.getString("ATTENDNO") + "  ");
                    System.out.println(SqlResult.getString("NAME_SHOW") + "  "); 
                    /** 情報設定 */
                    StrGrade    = SqlResult.getString("GRADE");
                    StrClass    = SqlResult.getString("HR_CLASS");
                    StrAttendNo = SqlResult.getString("ATTENDNO");
                    StrName     = SqlResult.getString("NAME_SHOW");
                    StrSchregno = SqlResult.getString("SCHREGNO");

                    /** 生徒情報格納 */
                    setStudent(rpCnt,StrGrade,StrClass,StrAttendNo,StrName,StrSchregno);
                }
                /** COMMIT */
                db2.commit();


            }
            catch (Exception e)
            {
                System.out.println("生徒情報取得エラー = " + e.getMessage());
                break;
            }
        }

        /** 最大行数取得 */
        getMaxRow(AllColCnt);

        for(int i = 0;i < AllColCnt;i++)
        {
            System.out.println("Vector" + i + " = " + col_value[i].size());
        }
    }


    /** 列名の設定 **/
    public String getColumnName(int columnIndex)
    {
        return Header[columnIndex];
    }

    private String getData(int row, int col)
    {
        String StrRet;
        if (row < VecCnt[col])
        {
            StudentData rowData = (StudentData)col_value[col].elementAt(row);
            StrRet = rowData.getData(0) + "-"
                   + rowData.getData(1) + " "
                   + rowData.getData(2) + "  "
                   + rowData.getData(3) + "  "
                   + rowData.getData(4);
        }
        else
        {
            /** データ件数をこした場合 */
            StrRet = "";
        }

        return StrRet;
    }

    public void setSelData(String SelYear, String SelSemester, String SelChrCD, String SelGrpCD, String SelStaff, String SelClass)
    {
        String WkIndex,WkGraCla;
        int SentoCnt,EndCnt,roopCnt;
        String wkGrade,wkClass;
        /** 列数取得 */
        for (roopCnt = 0,SentoCnt = 0,EndCnt = 4; ; roopCnt++)
        {
            try
            {
                SelClass.substring(SentoCnt,EndCnt);
                /** 文字列開始位置カウントアップ */
                SentoCnt = SentoCnt + 4;
                /** 文字列終了位置カウントアップ */
                EndCnt = EndCnt + 4;
            }
            catch(Exception e)
            {
                /** 該当データ無し */
                break;
            }
        }

        /** 対象クラス数設定 */
        AllColCnt = roopCnt;

        /** 配列作成 */
        col_value = new Vector[AllColCnt];
        for(int i = 0;i < AllColCnt;i++)
        {
            col_value[i] = new Vector();
        }
        VecCnt    = new int[AllColCnt];
        Header    = new String[AllColCnt];

        /** 変数初期化 */
        AllClear();

        /** 年度・群コード・群SEQ 設定 */
        Year    =   SelYear;
        Semester =  SelSemester;
        ChrCD   =   SelChrCD;
        GrpCD   =   SelGrpCD;
        stclass =   SelClass;
        StaffCD =   SelStaff; 

        SentoCnt = 0;
        EndCnt = 4;
        for (roopCnt = 0; ; roopCnt++)
        {
            try
            {
                WkIndex = String.valueOf(roopCnt);
                WkGraCla = SelClass.substring(SentoCnt,EndCnt);
                /** 対象クラス編集(学年，級) */
                wkGrade = WkGraCla.substring(0,2);
                wkClass = WkGraCla.substring(2,4);
                /** データ設定 */
                Header[roopCnt] = wkGrade + "年" + wkClass + "組";
                /** 文字列開始位置カウントアップ */
                SentoCnt = SentoCnt + 4;
                /** 文字列終了位置カウントアップ */
                EndCnt = EndCnt + 4;
            }
            catch(Exception e)
            {
                /** 該当データ無し */
                break;
            }
        }
    }

    /**
     * 変数初期化
     */
    public void AllClear()
    {
        for (int cnt = 0; cnt < AllColCnt; cnt++)
        {
            /** ヘッダ初期化 */
            Header[cnt] = "";
            /** 行数初期化 */
            VecCnt[cnt] = 0;
            /** VectorClass 初期化 */
            col_value[cnt].removeAllElements();
        }
    }

    /**
     * 生徒情報詳細設定
     */
    public void setStudent(int NowCnt,String StrGrade,String StrClass,
                           String StrAttendNo,String StrName, String StrSchregno)
    {
        StudentData Student = new StudentData();
        Student.setGrade_no(StrGrade);
        Student.setClass_no(StrClass);
        Student.setAttend_no(StrAttendNo);
        Student.setName(StrName);
        Student.setSchregno(StrSchregno);
        Student.setRegisterCD(StaffCD);
        col_value[NowCnt].addElement(Student);
        VecCnt[NowCnt]++;
    }

    /**
     * 最大行数取得
     */
    public void getMaxRow(int MaxCol)
    {
        for (int Cnt = 0; Cnt < MaxCol; Cnt++)
        {
            if(Cnt == 0)
            {
                AllRowCnt = col_value[Cnt].size();
            }
            else
            {
                if (col_value[Cnt].size() > AllRowCnt)
                {
                    AllRowCnt = col_value[Cnt].size();
                }
            }
        }
    }

    /**
     * 移動生徒情報取得・削除
     */
    public StudentData getStudentData(int row, int col)
    {
        StudentData VecResult = null;
        VecResult = (StudentData)col_value[col].elementAt(row);
        col_value[col].removeElementAt(row);
        VecCnt[col]--;
        return VecResult;
    }

    /**
     * 年・組取得
     */
    public String getGradeClass(int row, int col)
    {
        String StrRet2;
        if (row < VecCnt[col])
        {
            StudentData rowData = (StudentData)col_value[col].elementAt(row);
            StrRet2 = rowData.getData(0)
                    + rowData.getData(1);
        }
        else
        {
            /** データ件数をこした場合 */
            StrRet2 = "";
        }
        return StrRet2;
    }
}
