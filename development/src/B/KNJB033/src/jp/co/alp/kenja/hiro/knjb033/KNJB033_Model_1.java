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

import java.util.Vector;

import java.lang.String;

import javax.swing.table.DefaultTableModel;

import java.sql.ResultSet;

import nao_package.db.DB2UDB;

/**
 * �^�C�g��:  ���Ԋ��E���C�Ǘ��V�X�e��
 * ����:
 * ���쌠:   Copyright (c) 2004
 * ��Ж�:
 * @author 
 * @version 1.0
 */

public class KNJB033_Model_1 extends DefaultTableModel
{
	DB2UDB      db2;        // Database�N���X���p�������N���X
	ResultSet   SqlResult1; // �c�a�̌�������
	ResultSet   SqlResult2; // �c�a�̌�������
    ResultSet   SqlResult3; // �c�a�̌�������
    ResultSet   SqlResult4; // �c�a�̌�������
    int         SR1Cnt;      // �c�a�̌������ʌ���
    String      sql;        // SQL�쐬�p
    int         AllColCnt;  // ��
    int         AllRowCnt;  // �s��
    String      Year;       // �I��N�x
    String      Semester;   // �I���w��
    String      ChrCD;      // �u���R�[�h
    String      GrpCD;      // �I���Q�R�[�h
    String      StrChaircd; // �u���R�[�h�{�u������
    String[]    StrKamoku;  // �ȖڃR�[�h�{�Ȗږ���
    String[]    StrStaff;   // �E���R�[�h�{�E������
    String[]    Hd_dat1;    // �w�b�_�P�ǉ����
    String[]    Hd_dat2;    // �w�b�_�Q�ǉ����
    static String[]    Hd_dat3;    // �w�b�_�R�ǉ����
    String[]    chaircd;    // ��u�N���X�R�[�h
    int[]       targetcol;  // �ۑ��Ώۗ�
    Vector[] Kamoku_dat;    // �Ȗڐ��k���
    int[] VecCnt;           // Vector Count

    /**
     * �R���X�g���N�^
     */
    public KNJB033_Model_1()
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

    public Object getValueAt(int parm1, int parm2)
    {
        /**@todo: ���� javax.swing.table.AbstractTableModel abstract ���\�b�h������*/
        String date = this.getData(parm1, parm2);
        return date;
    }

    public int getRowCount()
    {
        /**@todo: ���� javax.swing.table.AbstractTableModel abstract ���\�b�h������*/
        return AllRowCnt;
    }

////////////////////////////////////////////////////////////////////

    /**
     * ��u�N���X����f�[�^�Z�b�g
     */
    public void query(DB2UDB db2)
    {
        /** SQL���̍쐬 */
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
            /** SQL���̎��s */
            db2.query(sql);

            /** SQL���s���ʎ擾 */
            SqlResult1 = db2.getResultSet();

            /** ��u���k���f�[�^�z�񌏐��擾 */
            SR1Cnt = 0;
            SqlResult1.next();
            SR1Cnt = SqlResult1.getInt("CNT");
            db2.commit();
        }
        catch ( Exception e)
        {
            /** �f�[�^�擾�G���[ */
            System.out.println("��u�N���X���댏���f�[�^�擾�G���[");
        }


//        if(SR1Cnt != 0)
//        {
            /** ��u���k���f�[�^�z��ݒ� */
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
            /** SQL���̍쐬 */
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
            /** �f�[�^�擾�G���[ */
            System.out.println("�����f�[�^�擾�G���[");
        }

            try
            {
                /** SQL���̎��s */
                db2.query(sql);

                /** SQL���s���ʎ擾 */
                SqlResult1 = db2.getResultSet();

                AllColCnt = 0;
                int HdCnt = 0;
                String wrkChrCD = "0000000"; 
                // ��u�N���X�R�[�h������
                RemoveAttendclasscd();
                while( SqlResult1.next() )
                {
                    if(wrkChrCD.equals(SqlResult1.getString("CHAIRCD"))) {
                        Hd_dat3[HdCnt-1] += "," + SqlResult1.getString("STAFFCD")
                                   + " " + SqlResult1.getString("STAFFNAME_SHOW");
                        wrkChrCD = SqlResult1.getString("CHAIRCD");
                        continue;
                    }
                    /** �P�w�b�_���擾 */
                    Hd_dat1[HdCnt] = SqlResult1.getString("CHAIRCD")
                                  + " " + SqlResult1.getString("CHAIRNAME");
                    /** �Q�w�b�_���擾 */
                    Hd_dat2[HdCnt] = SqlResult1.getString("SUBCLASSCD")
                                  + " " + SqlResult1.getString("SUBCLASSNAME");
                    /** �R�w�b�_���擾 */
                    Hd_dat3[HdCnt] = SqlResult1.getString("STAFFCD")
                                   + " " + SqlResult1.getString("STAFFNAME_SHOW");
                    wrkChrCD = SqlResult1.getString("CHAIRCD");
                    // ��u�N���X�R�[�h�擾
                    SetAttendclasscd(HdCnt,SqlResult1.getString("CHAIRCD"));
                    /** �J�E���^�J�E���g�A�b�v */
                    HdCnt++;
                    /** �񐔃J�E���g�A�b�v */
                    AllColCnt++;
                }
                /** COMMIT */
                db2.commit();
            }
            catch ( Exception e)
            {
                /** �f�[�^�擾�G���[ */
                System.out.println("��u�N���X����f�[�^�擾�G���[");
            }
//        }

        /** ���k���擾 */
        /** SQL���쐬 */
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
System.out.println("jTable1 ���sSQL = " + sql);
        try
        {
            /** SQL���̎��s */
            db2.query(sql);
            /** ���s���ʎ擾 */
            SqlResult2 = db2.getResultSet();

            /** �ϐ������� */
            AllClear();

            int count = 0; // 12/8�ǉ�
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
                /** ���ݒ� */
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
                /** �J�n�t���O���� */
                if (StrFlg == true)
                {
                    /** �ŏ��̃f�[�^ */
                    NowAttend = SqlResult2.getString("CHAIRCD");
                    for(int colcnt = 0;colcnt < SR1Cnt;colcnt++)
                    {
                        if(NowAttend.equals(chaircd[colcnt]))
                        {
                            /** ���k���i�[ */
                            setStudent(colcnt,StrGrade,StrClass,StrAttendNo,StrName,StrSchregno,StrRow,StrColumn,StrAppDate,StrAppEndDate,StrRegisterCD);
                            StrFlg = false;
                        }
                    }
                }
                else
                {
                    /** ��u�N���X�R�[�h���� */
                    if (NowAttend.equals((SqlResult2.getString("CHAIRCD"))))
                    {
                        for(int colcnt = 0;colcnt < SR1Cnt;colcnt++)
                        {
                            if(NowAttend.equals(chaircd[colcnt]))
                            {
                            /** �����u�N���X */
                            /** ���k���i�[ */
                                setStudent(colcnt,StrGrade,StrClass,StrAttendNo,StrName,StrSchregno,StrRow,StrColumn,StrAppDate,StrAppEndDate,StrRegisterCD);
                            }
                        }
                    }
                    else
                    {
                        /** �ʎ�u�N���X */
                        NowAttend = SqlResult2.getString("CHAIRCD");
                        for(int colcnt = 0;colcnt < SR1Cnt;colcnt++)
                        {
                            if(NowAttend.equals(chaircd[colcnt]))
                            {
                                /** ���k���i�[ */
                                setStudent(colcnt,StrGrade,StrClass,StrAttendNo,StrName,StrSchregno,StrRow,StrColumn,StrAppDate,StrAppEndDate,StrRegisterCD);
                            }
                        }
                    }
                }
            }
            /** COMMIT */
            db2.commit();

            /** �ő�s���擾 */
            getMaxRow(SR1Cnt);
        }
        catch (Exception f)
        {
            System.out.println("���k���擾�G���[ = " + f.getMessage());
        }
    }

    /** �񖼂̐ݒ� **/
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

    /** �񖼂̐ݒ� **/
    public static String getColumnName2(int columnIndex){
        return Hd_dat3[columnIndex];
    }

    /**
     * �e�[�u���w�b�_�ǉ����擾
     */
    public String getHeadAdd(int Cnt)
    {
        return Hd_dat1[Cnt];
    }
    /**
     * �e�[�u���w�b�_�ǉ����擾
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
            /** �f�[�^�������������ꍇ */
            StrRet = "";
        }
        return StrRet;
    }

    /**
     * �I�����ݒ�
     */
    public void setSelData(String SelYear, String SelSemester, String SelChrCD, String SelGrpCD)
    {
        /** �N�x�E�Q�R�[�h�E�QSEQ �ݒ� */
        Year    =   SelYear;
        ChrCD   =   SelChrCD;
        GrpCD   =   SelGrpCD;
        Semester =  SelSemester;
    }

    /**
     * ���k���ڍאݒ�
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
     * �ϐ�������
     */
    public void AllClear()
    {
            /** �s�������� */
            VecCnt[0] = 0;

            /** VectorClass ������ */
            Kamoku_dat[0].removeAllElements();

        for(int i = 0;i < SR1Cnt;i++)
        {
            /** �s�������� */
            VecCnt[i] = 0;

            /** VectorClass ������ */
            Kamoku_dat[i].removeAllElements();
        }
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
     * �ړ����k���擾�E�폜
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
     * �ۑ��Ώۗ�ݒ�
     */
    public void SetTargetCol(int num,int value)
    {

        targetcol[num] = value;

    }

    /**
     * ��u�N���X�R�[�h�ݒ�
     */
    public void SetAttendclasscd(int num,String value)
    {
        chaircd[num] = value;
    }

    /**
     * �u���R�[�h������
     */
    public void RemoveAttendclasscd()
    {
       for(int i=0;i<SR1Cnt;i++)
       {
           chaircd[i] = "";
       }
    }

    /**
     * DB�ւ̃f�[�^�X�V
     */
    public boolean setDB(DB2UDB db2)
    {
        String attendclasscd;
        String dsql = "";
        String isql = "";

        int targetCnt = 0;
        int colcnt = 0;
        int row = 1;        // �s
        int column = 1;     // ��
        String grade;       // �w�N
        String hr_class;    // �N���X
        String attendno;    // �o�Ȕԍ�
        String lname;       // ���k��
        String fname;       // ���k��
        String schregno;    // �w�Дԍ�
        boolean CheckFlg;   // �ۑ������m�F�t���O
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
                // ��u�N���X�f�[�^�̍폜(�����͍u���R�[�h)

                dsql = "delete "
                     + "from "
                     + "chair_std_dat "
                     + "where YEAR = '" + Year + "' "
                     + "and SEMESTER = '" + Semester + "' "
                     + "and chaircd='" + chaircd[roopcnt] + "'"
                     + "and APPDATE = (select CTRL_DATE from control_mst where CTRL_NO = '01') "; 

                System.out.println((roopcnt + 1) + "���delete SQL�m�F " + dsql);
                try
                {
                    RetCheck = db2.executeUpdate(dsql);
                    // �ُ�I��
                    if(RetCheck == 0)
                    {
                        System.out.println("�V�K�쐬");
                    }
                }
                catch (Exception g)
                {
                    System.out.println("���k���폜�G���[ " + g.getMessage());
                    return false;
                }

                // ��u�N���X�f�[�^�̏I�����t�̍X�V

                dsql = "update "
                     + "chair_std_dat "
                     + "set APPENDDATE = (select CTRL_DATE from control_mst where CTRL_NO = '01') - 1 DAY " 
                     + "where YEAR = '" + Year + "' "
                     + "and SEMESTER = '" + Semester + "' "
                     + "and chaircd='" + chaircd[roopcnt] + "'";

                System.out.println((roopcnt + 1) + "���update SQL�m�F " + dsql);
                try
                {
                    RetCheck = db2.executeUpdate(dsql);
                    // �ُ�I��
                    if(RetCheck == 0)
                    {
                        System.out.println("�V�K�쐬");
                    }
                }
                catch (Exception g)
                {
                    System.out.println("���k���X�V�G���[ " + g.getMessage());
                    return false;
                }

                /** COMMIT */
                db2.commit();

                // �f�[�^�o�^
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
                    // 6�s�ŐܕԂ�,�񐔃J�E���g�A�b�v
                    if(row == 6)
                    {
                        row = 1;
                        column++;
                    }
                    try
                    {
System.out.println("SQL�`�F�b�N " + isql);
                        /** SQL���̎��s */
                        RetCheck = db2.executeUpdate(isql);
                        // �ُ�I��
                        if(RetCheck == 0)
                        {
                            return false;
                        }
                        System.out.println(rowData.getData(9));
                    }
                    catch (Exception f)
                    {
                        System.out.println("���k���o�^�G���[ = " + f.getMessage());
                        return false;
                    }
                }
                /** COMMIT */
                db2.commit();
            }
        }
        // ����I��
        return true;
    }
}
