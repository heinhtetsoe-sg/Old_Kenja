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

public class StudentData implements Cloneable{

    String grade_no;    // �w�N
    String class_no;    // �g
    String attend_no;   // �o�Ȕԍ�
    String name;        // ����(��)
    String schregno;    // �w�Дԍ�
    String year;        // �N�x
    String sdate;       // �J�n��
    String edate;       // �I����
    String row;         // �s
    String column;      // ��
    String registercd;  // �ݒ�E��

    // �R���X�g���N�^
    public StudentData()
    {
        grade_no = "0";
        class_no = "0";
        attend_no = "0";
        name      = "����(��)";
        schregno = "�w�Дԍ�";
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
            //�����ŃG���[����
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
     * �w�N�擾
     */
    public void setGrade_no(String value)
    {
        grade_no = value;
    }

    /**
     * �N���X�擾
     */
    public void setClass_no(String value)
    {
        class_no = value;
    }

    /**
     * �o�Ȕԍ��擾
     */
    public void setAttend_no(String value)
    {
        attend_no = value;
    }

    /**
     * ����(��)�擾
     */
    public void setName(String value)
    {
        name = value;
    }
// kawataAdd 12/7
    /**
     * �w�Дԍ��擾
     */
    public void setSchregno(String value)
    {
        schregno = value;
    }
// kawataAddEnd 12/7
    /**
     * �N�x�擾
     */
    public void setYear(String value)
    {
        year = value;
    }
    /**
     * �s�擾
     */
    public void setRow(String value)
    {
        row = value;
    }
    /**
     *��擾
     */
    public void setColumn(String value)
    {
        column = value;
    }
    /**
     *���k�J�n�擾
     */
    public void setAppDate(String value)
    {
        sdate = value;
    }
    /**
     *���k�I���擾
     */
    public void setAppEndDate(String value)
    {
        edate = value;
    }
    /**
     *�ݒ�E���擾
     */
    public void setRegisterCD(String value)
    {
        registercd = value;
    }
}