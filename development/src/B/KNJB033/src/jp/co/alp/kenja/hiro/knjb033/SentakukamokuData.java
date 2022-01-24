// kanji=����
/*
 * $Id$
 *
 * �쐬��: 2004/06/09 15:22:29 - JST
 * �쐬��: teruya
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.hiro.knjb033;

public class SentakukamokuData implements Cloneable
{

// kawataAdd 12/13
    int GroupCD;              // �S�R�[�h
// kawataAddEnd 12/13
    String henko_kb;          // �ύX�敪
    String kamoku_name;       // �Ȗږ�
    String kaime;             // ���
    String kikan_start;       // ���{���ԁi�J�n�j
    String kikan_end;         // ���{���ԁi�I���j
    String TaiClass;          // �ΏۃN���X

    // �R���X�g���N�^
    public SentakukamokuData()
    {
        henko_kb = "";
        kamoku_name = "�Ȗږ�";
        kaime = "1";
        kikan_start = "���ԊJ�n";
        kikan_end = "���ԏI��";
        TaiClass = "�ΏۃN���X";
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
            //�����ŃG���[����
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