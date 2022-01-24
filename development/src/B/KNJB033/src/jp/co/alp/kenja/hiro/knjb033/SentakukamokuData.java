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

public class SentakukamokuData implements Cloneable
{

// kawataAdd 12/13
    int GroupCD;              // 郡コード
// kawataAddEnd 12/13
    String henko_kb;          // 変更区分
    String kamoku_name;       // 科目名
    String kaime;             // 回目
    String kikan_start;       // 実施期間（開始）
    String kikan_end;         // 実施期間（終了）
    String TaiClass;          // 対象クラス

    // コンストラクタ
    public SentakukamokuData()
    {
        henko_kb = "";
        kamoku_name = "科目名";
        kaime = "1";
        kikan_start = "期間開始";
        kikan_end = "期間終了";
        TaiClass = "対象クラス";
    }

  //////
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
  ///
    public String getKamoku_name()
    {
        return kamoku_name;
    }
    public String getData(int index)
    {
        String str;
        switch (index)
        {
            case 0:
                str = henko_kb;
                break;
            case 1:
                str = kamoku_name;
                break;
            case 2:
                str = kaime;
                break;
            case 3:
                str = kikan_start;
                break;
            case 4:
                str = kikan_end;
                break;
            case 5:
                str = TaiClass;
                break;
            default:
                str = "";
        }
        return str;
    }

// kawataAdd 12/13
    public int getGroupCD()
    {
        return GroupCD;
    }
    public void setGroupCD(int value)
    {
        GroupCD = value;
    }
// kwataAddEnd 12/13
    public void setHenko_kb(String value)
    {
        henko_kb = value;
    }
    public void setKamoku_name(String value)
    {
        kamoku_name = value;
    }
    public void setKaime(String value)
    {
        kaime = value;
    }
    public void setKikan_start(String value)
    {
        kikan_start = value;
    }
    public void setKikan_end(String value)
    {
        kikan_end = value;
    }
    public void setTaiClass(String value)
    {
        TaiClass = value;
    }
}