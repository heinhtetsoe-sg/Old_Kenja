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

import java.util.Vector;

import java.lang.String;

import javax.swing.table.DefaultTableModel;

import java.sql.ResultSet;

import nao_package.db.DB2UDB;

/**
 * タイトル:  時間割・履修管理システム
 * 説明:
 * 著作権:   Copyright (c) 2004
 * 会社名:
 * @author 
 * @version 1.0
 */

public class KNJB033_Model_1 extends DefaultTableModel
{
	DB2UDB      db2;        // Databaseクラスを継承したクラス
	ResultSet   SqlResult1; // ＤＢの検索結果
	ResultSet   SqlResult2; // ＤＢの検索結果
    ResultSet   SqlResult3; // ＤＢの検索結果
    ResultSet   SqlResult4; // ＤＢの検索結果
    int         SR1Cnt;      // ＤＢの検索結果件数
    String      sql;        // SQL作成用
    int         AllColCnt;  // 列数
    int         AllRowCnt;  // 行数
    String      Year;       // 選択年度
    String      Semester;   // 選択学期
    String      ChrCD;      // 講座コード
    String      GrpCD;      // 選択群コード
    String      StrChaircd; // 講座コード＋講座名称
    String[]    StrKamoku;  // 科目コード＋科目名称
    String[]    StrStaff;   // 職員コード＋職員名称
    String[]    Hd_dat1;    // ヘッダ１追加情報
    String[]    Hd_dat2;    // ヘッダ２追加情報
    static String[]    Hd_dat3;    // ヘッダ３追加情報
    String[]    chaircd;    // 受講クラスコード
    int[]       targetcol;  // 保存対象列
    Vector[] Kamoku_dat;    // 科目生徒情報
    int[] VecCnt;           // Vector Count

    /**
     * コンストラクタ
     */
    public KNJB033_Model_1()
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

    public Object getValueAt(int parm1, int parm2)
    {
        /**@todo: この javax.swing.table.AbstractTableModel abstract メソッドを実装*/
        String date = this.getData(parm1, parm2);
        return date;
    }

    public int getRowCount()
    {
        /**@todo: この javax.swing.table.AbstractTableModel abstract メソッドを実装*/
        return AllRowCnt;
    }

////////////////////////////////////////////////////////////////////

    /**
     * 受講クラス名簿データセット
     */
    public void query(DB2UDB db2)
    {
        /** SQL文の作成 */
        sql = "select "
            + "COUNT(*) CNT "
            + "from "
            + "CHAIR_DAT T1 "
//            + "v_subclass_mst T2, "
//            + "staff_mst T3, "
            + "where "
            + "T1.YEAR='" + Year + "' "
            + "and T1.GROUPCD='" + GrpCD + "' ";
//            + "and T2.YEAR='" + Year + "' "
//            + "and T1.SUBCLASSCD=T2.SUBCLASSCD "
//            + "and T1.STAFFCD=T3.STAFFCD "
//            + "and T1.ATTENDCLASSCD=T4.ATTENDCLASSCD";
                System.out.println("model1 SQL is " + sql);
        try
        {
            /** SQL文の実行 */
            db2.query(sql);

            /** SQL実行結果取得 */
            SqlResult1 = db2.getResultSet();

            /** 受講生徒名データ配列件数取得 */
            SR1Cnt = 0;
            SqlResult1.next();
            SR1Cnt = SqlResult1.getInt("CNT");
            db2.commit();
        }
        catch ( Exception e)
        {
            /** データ取得エラー */
            System.out.println("受講クラス名簿件数データ取得エラー");
        }


//        if(SR1Cnt != 0)
//        {
            /** 受講生徒名データ配列設定 */
            Hd_dat1 = new String[SR1Cnt];
            Hd_dat2 = new String[SR1Cnt];
            Hd_dat3 = new String[SR1Cnt];
            chaircd = new String[SR1Cnt];
            targetcol = new int[SR1Cnt];
            Kamoku_dat = new Vector[SR1Cnt];
            for(int i = 0;i<SR1Cnt;i++){
              Kamoku_dat[i] = new Vector();
            }
            VecCnt = new int[SR1Cnt];

        try
        {
            /** SQL文の作成 */
            sql = "SELECT "
                + "    T1.SUBCLASSCD, "
                + "    T2.SUBCLASSNAME, "
                + "    T3.STAFFCD, "
                + "    T4.STAFFNAME_SHOW, "
                + "    T3.CHAIRCD, "
                + "    T1.CHAIRNAME "
                + "FROM "
                + "    chair_dat T1, "
                + "    v_subclass_mst T2, "
                + "    chair_stf_dat T3, "
                + "    staff_mst T4 "
                + "WHERE "
                + "    T3.YEAR='" + Year + "' AND ";
             if(GrpCD.equals("0000")) {
                sql += "    T1.GROUPCD='0000'  AND "
                    +  "    T1.CHAIRCD='" + ChrCD + "'  AND ";
             }
             else {
                sql += "    T1.GROUPCD='" + GrpCD + "'  AND ";
             }
             sql += "    T2.YEAR='" + Year + "' AND "
                + "    T1.SUBCLASSCD=T2.SUBCLASSCD AND "
                + "    T3.STAFFCD=T4.STAFFCD AND "
                + "    T1.YEAR='" + Year + "' AND "
                + "    T1.SEMESTER = '" + Semester + "' AND "
                + "    T1.SEMESTER = T3.SEMESTER AND "
                + "    T1.chaircd=T3.chaircd "
                + "ORDER BY "
                + "    1, "
                + "    3,5 ";
                System.out.println("model1 SQL is " + sql);
        }
        catch ( Exception e)
        {
            /** データ取得エラー */
            System.out.println("初期データ取得エラー");
        }

            try
            {
                /** SQL文の実行 */
                db2.query(sql);

                /** SQL実行結果取得 */
                SqlResult1 = db2.getResultSet();

                AllColCnt = 0;
                int HdCnt = 0;
                String wrkChrCD = "0000000"; 
                // 受講クラスコード初期化
                RemoveAttendclasscd();
                while( SqlResult1.next() )
                {
                    if(wrkChrCD.equals(SqlResult1.getString("CHAIRCD"))) {
                        Hd_dat3[HdCnt-1] += "," + SqlResult1.getString("STAFFCD")
                                   + " " + SqlResult1.getString("STAFFNAME_SHOW");
                        wrkChrCD = SqlResult1.getString("CHAIRCD");
                        continue;
                    }
                    /** １ヘッダ情報取得 */
                    Hd_dat1[HdCnt] = SqlResult1.getString("CHAIRCD")
                                  + " " + SqlResult1.getString("CHAIRNAME");
                    /** ２ヘッダ情報取得 */
                    Hd_dat2[HdCnt] = SqlResult1.getString("SUBCLASSCD")
                                  + " " + SqlResult1.getString("SUBCLASSNAME");
                    /** ３ヘッダ情報取得 */
                    Hd_dat3[HdCnt] = SqlResult1.getString("STAFFCD")
                                   + " " + SqlResult1.getString("STAFFNAME_SHOW");
                    wrkChrCD = SqlResult1.getString("CHAIRCD");
                    // 受講クラスコード取得
                    SetAttendclasscd(HdCnt,SqlResult1.getString("CHAIRCD"));
                    /** カウンタカウントアップ */
                    HdCnt++;
                    /** 列数カウントアップ */
                    AllColCnt++;
                }
                /** COMMIT */
                db2.commit();
            }
            catch ( Exception e)
            {
                /** データ取得エラー */
                System.out.println("受講クラス名簿データ取得エラー");
            }
//        }

        /** 生徒情報取得 */
        /** SQL文作成 */
        sql = "select "
            + "T1.CHAIRCD, "
            + "T3.GRADE, "
            + "T3.HR_CLASS, "
            + "T3.SCHREGNO, "
            + "T3.ATTENDNO, "
            + "T4.NAME_SHOW, "
            + "T2.ROW, "
            + "T2.COLUMN, "
            + "T2.APPDATE, "
            + "T2.APPENDDATE, "
            + "T2.REGISTERCD "
            + "from "
            + "CHAIR_DAT T1, "
            + "CHAIR_STD_DAT T2, "
            + "schreg_regd_dat T3, "
            + "schreg_base_mst T4 "
            + "where "
            + "T1.YEAR = '" + Year + "' "
            + "and T1.SEMESTER = '" + Semester + "' "
            + "and T1.SEMESTER = T2.SEMESTER "
            + "and T1.SEMESTER = T3.SEMESTER "
            + "and T1.GROUPCD='" + GrpCD + "' "
            + "and T2.YEAR='" + Year + "' "
            + "and T1.CHAIRCD = T2.CHAIRCD "
            + "and T3.YEAR = '" + Year + "' "
            + "and T1.SEMESTER = T3.SEMESTER "
            + "and T2.SCHREGNO = T3.SCHREGNO "
            + "and T2.SCHREGNO = T4.SCHREGNO "
                + "and ((select CTRL_DATE from control_mst where CTRL_NO = '01') between T2.APPDATE and T2.APPENDDATE or T2.APPDATE is null)"
            + "order by T1.CHAIRCD, T3.GRADE, "
            + "T3.HR_CLASS, T3.ATTENDNO";
System.out.println("jTable1 発行SQL = " + sql);
        try
        {
            /** SQL文の実行 */
            db2.query(sql);
            /** 実行結果取得 */
            SqlResult2 = db2.getResultSet();

            /** 変数初期化 */
            AllClear();

            int count = 0; // 12/8追加
            boolean StrFlg = true;
            String NowAttend = "";
            String StrAttend = "";
            String StrGrade;
            String StrClass;
            String StrAttendNo;
            String StrName;
            String StrSchregno;
            String StrRow;
            String StrColumn;
            String StrAppDate;
            String StrAppEndDate;
            String StrRegisterCD;
            
            while( SqlResult2.next() )
            {
                count++;
                /** 情報設定 */
                StrChaircd  = SqlResult2.getString("CHAIRCD");
                StrGrade    = SqlResult2.getString("GRADE");
                StrClass    = SqlResult2.getString("HR_CLASS");
                StrAttendNo = SqlResult2.getString("ATTENDNO");
                StrName     = SqlResult2.getString("NAME_SHOW");
                StrSchregno = SqlResult2.getString("SCHREGNO");
                StrRow      = SqlResult2.getString("ROW");
                StrColumn   = SqlResult2.getString("COLUMN");
                StrAppDate  = SqlResult2.getString("APPDATE");
                StrAppEndDate= SqlResult2.getString("APPENDDATE");
                StrRegisterCD= SqlResult2.getString("REGISTERCD");
                /** 開始フラグ判定 */
                if (StrFlg == true)
                {
                    /** 最初のデータ */
                    NowAttend = SqlResult2.getString("CHAIRCD");
                    for(int colcnt = 0;colcnt < SR1Cnt;colcnt++)
                    {
                        if(NowAttend.equals(chaircd[colcnt]))
                        {
                            /** 生徒情報格納 */
                            setStudent(colcnt,StrGrade,StrClass,StrAttendNo,StrName,StrSchregno,StrRow,StrColumn,StrAppDate,StrAppEndDate,StrRegisterCD);
                            StrFlg = false;
                        }
                    }
                }
                else
                {
                    /** 受講クラスコード判定 */
                    if (NowAttend.equals((SqlResult2.getString("CHAIRCD"))))
                    {
                        for(int colcnt = 0;colcnt < SR1Cnt;colcnt++)
                        {
                            if(NowAttend.equals(chaircd[colcnt]))
                            {
                            /** 同一受講クラス */
                            /** 生徒情報格納 */
                                setStudent(colcnt,StrGrade,StrClass,StrAttendNo,StrName,StrSchregno,StrRow,StrColumn,StrAppDate,StrAppEndDate,StrRegisterCD);
                            }
                        }
                    }
                    else
                    {
                        /** 別受講クラス */
                        NowAttend = SqlResult2.getString("CHAIRCD");
                        for(int colcnt = 0;colcnt < SR1Cnt;colcnt++)
                        {
                            if(NowAttend.equals(chaircd[colcnt]))
                            {
                                /** 生徒情報格納 */
                                setStudent(colcnt,StrGrade,StrClass,StrAttendNo,StrName,StrSchregno,StrRow,StrColumn,StrAppDate,StrAppEndDate,StrRegisterCD);
                            }
                        }
                    }
                }
            }
            /** COMMIT */
            db2.commit();

            /** 最大行数取得 */
            getMaxRow(SR1Cnt);
        }
        catch (Exception f)
        {
            System.out.println("生徒情報取得エラー = " + f.getMessage());
        }
    }

    /** 列名の設定 **/
    public String getColumnName(int columnIndex){
        String [] aaa;
        String bbb = "<html>";
        aaa = Hd_dat3[columnIndex].split(",");
        for(int i=0;i<aaa.length;i++) {
            if(i!=0) {
                bbb += "<BR>";
            }
            bbb += aaa[i];
        }
        return bbb;
    }

    /** 列名の設定 **/
    public static String getColumnName2(int columnIndex){
        return Hd_dat3[columnIndex];
    }

    /**
     * テーブルヘッダ追加情報取得
     */
    public String getHeadAdd(int Cnt)
    {
        return Hd_dat1[Cnt];
    }
    /**
     * テーブルヘッダ追加情報取得
     */
    public String getHeadAdd2(int Cnt)
    {
        return Hd_dat2[Cnt];
    }

    private String getData(int row, int col)
    {
        String StrRet;

        if (row < VecCnt[col])
        {
            StudentData rowData = (StudentData)Kamoku_dat[col].elementAt(row);
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

    /**
     * 選択情報設定
     */
    public void setSelData(String SelYear, String SelSemester, String SelChrCD, String SelGrpCD)
    {
        /** 年度・群コード・群SEQ 設定 */
        Year    =   SelYear;
        ChrCD   =   SelChrCD;
        GrpCD   =   SelGrpCD;
        Semester =  SelSemester;
    }

    /**
     * 生徒情報詳細設定
     */
    public void setStudent(int NowCnt,String StrGrade,String StrClass,
                           String StrAttendNo,String StrName,String StrSchregno,
                           String StrRow,String StrColumn,String StrAppDate,String StrAppEndDate,String StrRegisterCD)
    {
        StudentData Student = new StudentData();
        Student.setGrade_no(StrGrade);
        Student.setClass_no(StrClass);
        Student.setAttend_no(StrAttendNo);
        Student.setName(StrName);
        Student.setSchregno(StrSchregno);
        Student.setRow(StrRow);
        Student.setColumn(StrColumn);
        Student.setAppDate(StrAppDate);
        Student.setAppEndDate(StrAppEndDate);
        Student.setRegisterCD(StrRegisterCD);
        Kamoku_dat[NowCnt].addElement(Student);
        VecCnt[NowCnt]++;

    }

    /**
     * 変数初期化
     */
    public void AllClear()
    {
            /** 行数初期化 */
            VecCnt[0] = 0;

            /** VectorClass 初期化 */
            Kamoku_dat[0].removeAllElements();

        for(int i = 0;i < SR1Cnt;i++)
        {
            /** 行数初期化 */
            VecCnt[i] = 0;

            /** VectorClass 初期化 */
            Kamoku_dat[i].removeAllElements();
        }
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
                AllRowCnt = Kamoku_dat[Cnt].size();
            }
            else
            {
                if (Kamoku_dat[Cnt].size() > AllRowCnt)
                {
                    AllRowCnt = Kamoku_dat[Cnt].size();
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

        VecResult = (StudentData)Kamoku_dat[col].elementAt(row);
        Kamoku_dat[col].removeElementAt(row);
        VecCnt[col]--;

        return VecResult;
    }

    /**
     * 保存対象列設定
     */
    public void SetTargetCol(int num,int value)
    {

        targetcol[num] = value;

    }

    /**
     * 受講クラスコード設定
     */
    public void SetAttendclasscd(int num,String value)
    {
        chaircd[num] = value;
    }

    /**
     * 講座コード初期化
     */
    public void RemoveAttendclasscd()
    {
       for(int i=0;i<SR1Cnt;i++)
       {
           chaircd[i] = "";
       }
    }

    /**
     * DBへのデータ更新
     */
    public boolean setDB(DB2UDB db2)
    {
        String attendclasscd;
        String dsql = "";
        String isql = "";

        int targetCnt = 0;
        int colcnt = 0;
        int row = 1;        // 行
        int column = 1;     // 列
        String grade;       // 学年
        String hr_class;    // クラス
        String attendno;    // 出席番号
        String lname;       // 生徒姓
        String fname;       // 生徒名
        String schregno;    // 学籍番号
        boolean CheckFlg;   // 保存完了確認フラグ
        int RetCheck = 1;
        for(int cnt = 0;cnt<AllColCnt;cnt++)
        {
            if(chaircd[cnt] != "" || chaircd[cnt] != null)
            {
                colcnt++;
            }
        }

        for(int roopcnt = 0;roopcnt<colcnt;roopcnt++)
        {
            int deleteCnt;
            row = 1;
            column = 1;
            dsql = "";
            isql = "";

            if(VecCnt[roopcnt] >= 0)
            {
                // 受講クラスデータの削除(条件は講座コード)

                dsql = "delete "
                     + "from "
                     + "chair_std_dat "
                     + "where YEAR = '" + Year + "' "
                     + "and SEMESTER = '" + Semester + "' "
                     + "and chaircd='" + chaircd[roopcnt] + "'"
                     + "and APPDATE = (select CTRL_DATE from control_mst where CTRL_NO = '01') "; 

                System.out.println((roopcnt + 1) + "列目delete SQL確認 " + dsql);
                try
                {
                    RetCheck = db2.executeUpdate(dsql);
                    // 異常終了
                    if(RetCheck == 0)
                    {
                        System.out.println("新規作成");
                    }
                }
                catch (Exception g)
                {
                    System.out.println("生徒情報削除エラー " + g.getMessage());
                    return false;
                }

                // 受講クラスデータの終了日付の更新

                dsql = "update "
                     + "chair_std_dat "
                     + "set APPENDDATE = (select CTRL_DATE from control_mst where CTRL_NO = '01') - 1 DAY " 
                     + "where YEAR = '" + Year + "' "
                     + "and SEMESTER = '" + Semester + "' "
                     + "and chaircd='" + chaircd[roopcnt] + "'";

                System.out.println((roopcnt + 1) + "列目update SQL確認 " + dsql);
                try
                {
                    RetCheck = db2.executeUpdate(dsql);
                    // 異常終了
                    if(RetCheck == 0)
                    {
                        System.out.println("新規作成");
                    }
                }
                catch (Exception g)
                {
                    System.out.println("生徒情報更新エラー " + g.getMessage());
                    return false;
                }

                /** COMMIT */
                db2.commit();

                // データ登録
                for(int cnt1 = 0;cnt1<VecCnt[roopcnt];cnt1++)
                {
                    StudentData rowData = (StudentData)Kamoku_dat[roopcnt].elementAt(cnt1);
                    schregno = rowData.getData(5);
                    isql = "insert "
                         + "into chair_std_dat "
                         + "values ('" 
                         + Year + "','" 
                         + Semester + "','" 
                         + chaircd[roopcnt] +"','" 
                         + schregno + "',"
                         + "(select CTRL_DATE from control_mst where CTRL_NO = '01') , "
                         + "(select EDATE from semester_mst where YEAR ='" + Year + "'AND SEMESTER = '" + Semester + "'),'"
                         + row + "','" 
                         + column + "','"
                         + KNJB033_Dlg.SelStaff+ "',"
                         + " CURRENT TIMESTAMP)";
                    row++;
                    // 6行で折返し,列数カウントアップ
                    if(row == 6)
                    {
                        row = 1;
                        column++;
                    }
                    try
                    {
System.out.println("SQLチェック " + isql);
                        /** SQL文の実行 */
                        RetCheck = db2.executeUpdate(isql);
                        // 異常終了
                        if(RetCheck == 0)
                        {
                            return false;
                        }
                        System.out.println(rowData.getData(9));
                    }
                    catch (Exception f)
                    {
                        System.out.println("生徒情報登録エラー = " + f.getMessage());
                        return false;
                    }
                }
                /** COMMIT */
                db2.commit();
            }
        }
        // 正常終了
        return true;
    }
}
