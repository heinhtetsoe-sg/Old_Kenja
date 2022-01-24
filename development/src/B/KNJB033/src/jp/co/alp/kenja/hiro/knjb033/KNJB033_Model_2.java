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

import javax.swing.table.*;
import java.util.*;
import java.sql.*;
import nao_package.db.*;

/**
 * �^�C�g��:  ���Ԋ��E���C�Ǘ��V�X�e��
 * ����:
 * ���쌠:   Copyright (c) 2004
 * ��Ж�:
 * @author m.miyagi
 * @version 1.0
 */

public class KNJB033_Model_2 extends DefaultTableModel {
    DB2UDB      db2;        // Database�N���X���p�������N���X
    ResultSet   SqlResult;  // �c�a�̌�������
    Vector[]    col_value;  // ��f�[�^
    int[]       VecCnt;     // �񐶓k��
    int         AllColCnt;  // ��
    int         AllRowCnt;  // �s��
    String      Year;       // �I��N�x
    String      Semester;   // �I���w��
    String      ChrCD;      // �I���u���R�[�h
    String      GrpCD;      // �I���Q�R�[�h
    String      StaffCD;    // ����E���R�[�h
    String      stclass;    // �ΏۃN���X
    String[]    Header;     // �w�b�_�\���p

    public KNJB033_Model_2()
    {
        super();
    }

    /**
     * �Z���ҏW�ݒ�
     */
    public boolean isCellEditable(int rowIndex,int colmunIndex)
    {
        /** �ҏW�s�� */
        return false;
    }

    public int getColumnCount()
    {
        /**@todo: ���� javax.swing.table.AbstractTableModel abstract ���\�b�h������*/
        return AllColCnt;
    }

    public Object getValueAt(int parm1, int parm2) {
        /**@todo: ���� javax.swing.table.AbstractTableModel abstract ���\�b�h������*/
        String date = this.getData(parm1, parm2);
        return date;
    }

    public int getRowCount()
    {
        /**@todo: ���� javax.swing.table.AbstractTableModel abstract ���\�b�h������*/

        // ��c�ǉ�����11/29
        return AllRowCnt + 1;
    }

////////////////////////////////////////////////////////////////////

    /** �N���X����f�[�^�Z�b�g */
    public void query(DB2UDB db2)
    {
        // ��c�ǉ����� 11/29
        String strgrade,strclass,strschregno,strlname,strfname;

        // �����l�N���A
        String sqlGrade = "";
        String sqlClass = "";
        String sql = "";

        // SQL�̐���
// tajimaAdd 12/11
        for (int rpCnt = 0; rpCnt < AllColCnt; rpCnt++)
//        for (int rpCnt = 0; rpCnt < (AllColCnt + 1); rpCnt++)
// tajimaAddEnd 12/11
        {
            try
            {
                /** �w�N���ݒ� */
                sqlGrade = Header[rpCnt].substring(0, 2);
                /** �g���ݒ� */
                sqlClass = Header[rpCnt].substring(3, 5);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                break;
            }
System.out.println("�w�N = " + sqlGrade);
System.out.println("�g = " + sqlClass);
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
                /** SQL���̎��s */
                db2.query(sql);
                /** ���s���ʎ擾 */
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
                    /** ���ݒ� */
                    StrGrade    = SqlResult.getString("GRADE");
                    StrClass    = SqlResult.getString("HR_CLASS");
                    StrAttendNo = SqlResult.getString("ATTENDNO");
                    StrName     = SqlResult.getString("NAME_SHOW");
                    StrSchregno = SqlResult.getString("SCHREGNO");

                    /** ���k���i�[ */
                    setStudent(rpCnt,StrGrade,StrClass,StrAttendNo,StrName,StrSchregno);
                }
                /** COMMIT */
                db2.commit();


            }
            catch (Exception e)
            {
                System.out.println("���k���擾�G���[ = " + e.getMessage());
                break;
            }
        }

        /** �ő�s���擾 */
        getMaxRow(AllColCnt);

        for(int i = 0;i < AllColCnt;i++)
        {
            System.out.println("Vector" + i + " = " + col_value[i].size());
        }
    }


    /** �񖼂̐ݒ� **/
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
            /** �f�[�^�������������ꍇ */
            StrRet = "";
        }

        return StrRet;
    }

    public void setSelData(String SelYear, String SelSemester, String SelChrCD, String SelGrpCD, String SelStaff, String SelClass)
    {
        String WkIndex,WkGraCla;
        int SentoCnt,EndCnt,roopCnt;
        String wkGrade,wkClass;
        /** �񐔎擾 */
        for (roopCnt = 0,SentoCnt = 0,EndCnt = 4; ; roopCnt++)
        {
            try
            {
                SelClass.substring(SentoCnt,EndCnt);
                /** ������J�n�ʒu�J�E���g�A�b�v */
                SentoCnt = SentoCnt + 4;
                /** ������I���ʒu�J�E���g�A�b�v */
                EndCnt = EndCnt + 4;
            }
            catch(Exception e)
            {
                /** �Y���f�[�^���� */
                break;
            }
        }

        /** �ΏۃN���X���ݒ� */
        AllColCnt = roopCnt;

        /** �z��쐬 */
        col_value = new Vector[AllColCnt];
        for(int i = 0;i < AllColCnt;i++)
        {
            col_value[i] = new Vector();
        }
        VecCnt    = new int[AllColCnt];
        Header    = new String[AllColCnt];

        /** �ϐ������� */
        AllClear();

        /** �N�x�E�Q�R�[�h�E�QSEQ �ݒ� */
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
                /** �ΏۃN���X�ҏW(�w�N�C��) */
                wkGrade = WkGraCla.substring(0,2);
                wkClass = WkGraCla.substring(2,4);
                /** �f�[�^�ݒ� */
                Header[roopCnt] = wkGrade + "�N" + wkClass + "�g";
                /** ������J�n�ʒu�J�E���g�A�b�v */
                SentoCnt = SentoCnt + 4;
                /** ������I���ʒu�J�E���g�A�b�v */
                EndCnt = EndCnt + 4;
            }
            catch(Exception e)
            {
                /** �Y���f�[�^���� */
                break;
            }
        }
    }

    /**
     * �ϐ�������
     */
    public void AllClear()
    {
        for (int cnt = 0; cnt < AllColCnt; cnt++)
        {
            /** �w�b�_������ */
            Header[cnt] = "";
            /** �s�������� */
            VecCnt[cnt] = 0;
            /** VectorClass ������ */
            col_value[cnt].removeAllElements();
        }
    }

    /**
     * ���k���ڍאݒ�
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
     * �ő�s���擾
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
     * �ړ����k���擾�E�폜
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
     * �N�E�g�擾
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
            /** �f�[�^�������������ꍇ */
            StrRet2 = "";
        }
        return StrRet2;
    }
}
