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

public class StudentData implements Cloneable{

    String grade_no;    // 学年
    String class_no;    // 組
    String attend_no;   // 出席番号
    String name;        // 氏名(名)
    String schregno;    // 学籍番号
    String year;        // 年度
    String sdate;       // 開始日
    String edate;       // 終了日
    String row;         // 行
    String column;      // 列
    String registercd;  // 設定職員

    // コンストラクタ
    public StudentData()
    {
        grade_no = "0";
        class_no = "0";
        attend_no = "0";
        name      = "氏名(名)";
        schregno = "学籍番号";
        year = "2000";
        sdate = null;
        edate = null;  
        row = "1";
        column = "1";
        registercd = "";
    }

    public Object clone()
    {
        try
        {
            SentakukamokuData wrk = (SentakukamokuData)super.clone();
            return wrk;
        }
        catch (CloneNotSupportedException e)
        {
            //ここでエラー処理
            throw new InternalError();
        }
    }

    public String getData(int index)
    {
        String str;
        switch (index)
        {
            case 0:
                str = grade_no;
                break;
            case 1:
                str = class_no;
                break;
            case 2:
                str = attend_no;
                break;
            case 3:
                str = name;
                break;
            case 4:
                str = "";
                break;
            case 5:
                str = schregno;
                break;
            case 6:
                str = year;
                break;
            case 7:
                str = sdate;
                break;
            case 8:
                str = edate;  
                break;
            case 9:
                str = row;
                break;
            case 10:
                str = column;
                break;
            case 11:
                str = registercd;
                break;
            default:
                str = "";
        }
        return str;
    }

    /**
     * 学年取得
     */
    public void setGrade_no(String value)
    {
        grade_no = value;
    }

    /**
     * クラス取得
     */
    public void setClass_no(String value)
    {
        class_no = value;
    }

    /**
     * 出席番号取得
     */
    public void setAttend_no(String value)
    {
        attend_no = value;
    }

    /**
     * 氏名(名)取得
     */
    public void setName(String value)
    {
        name = value;
    }
// kawataAdd 12/7
    /**
     * 学籍番号取得
     */
    public void setSchregno(String value)
    {
        schregno = value;
    }
// kawataAddEnd 12/7
    /**
     * 年度取得
     */
    public void setYear(String value)
    {
        year = value;
    }
    /**
     * 行取得
     */
    public void setRow(String value)
    {
        row = value;
    }
    /**
     *列取得
     */
    public void setColumn(String value)
    {
        column = value;
    }
    /**
     *生徒開始取得
     */
    public void setAppDate(String value)
    {
        sdate = value;
    }
    /**
     *生徒終了取得
     */
    public void setAppEndDate(String value)
    {
        edate = value;
    }
    /**
     *設定職員取得
     */
    public void setRegisterCD(String value)
    {
        registercd = value;
    }
}