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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import java.sql.ResultSet;

import nao_package.db.DB2UDB;

/**
 * ����̃R�s�[���(LHA020F3)
 * @author m.miyagi
 * @version 1.0
 */

public class KNJB033F1_Dlg extends JDialog {
    DB2UDB      db2;        // Database�N���X���p�������N���X
    ResultSet   SqlResult;  // �c�a�̌�������
    Vector data = new Vector();
//    Vector test = new Vector();
    int flag=0; //�R���{�{�b�N�X�\���p�t���O
    private boolean chkflg; // �I���t���O
    String subcode;  // �ȖڃR�[�h1
    String subcode2; // �ȖڃR�[�h2
    // �e�f�[�^�ێ��p�ϐ�
    String groupcd;      // �S�R�[�h
    String gname;        // �S����
    String TaiClass;     // �ΏۃN���X
    String year;  // �N�x
	String SelGrpCD; // �Q�R�[�h

     public Object clone(){
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

    /**
     * �Q�R�[�h�ݒ菈��
	 * @param	int
    */
    public void set_groupcd(String value){
        groupcd = value;
    }

    /**
     * �I���Ȗږ��̐ݒ菈��
	 * @param	String
    */
    public void set_gname(String value){
        gname = value;
    }

    /**
     * �ΏۃN���X�ݒ菈��
	 * @param	String
    */
    public void set_TaiClass(String value){
        TaiClass = value;
    }

    /**
     * �N�x�ݒ菈��
	 * @param	String
    */
    public void set_year(String value){
        year = value;
    }

	/**
	 * �I���Q�R�[�h�擾�֐�
	 * @param	����
	*/
	public String F3GetGrpCD()
	{
		return SelGrpCD;
	}

	/**
	 * �J�������擾�֐�
	 * @param	����
	*/
    public int getColCnt(){
        int lng=0;
        for(int i=0;i<f3pane.data1.length;i++)
        {
            if(f3pane.data1[i] != null && f3pane.data1[i] != "")
            {
                lng++;
            }
        }
        return lng;
    }

	/**
	 * �ΏۃJ�����擾�֐�
	 * @param	int
	*/
    public int gettargetColumn(int tcol)
    {
        int targetcol;
        targetcol = f3pane2.data5[tcol];
        return targetcol;
    }

	/**
	 * �S�R�[�h�擾�֐�
	 * @param	����
	*/
    public String getGrpCD()
    {
        return groupcd;
    }

	/**
	 * �ΏۃN���X�擾�֐�
	 * @param	����
	*/
    public String gettergetClass()
    {
        return TaiClass;
    }

	/**
	 * �Ώۃf�[�^�擾�֐�
	 * @param	����
	*/
    public String getData(int index,int count)
    {
        String data2;
        data2 = f3pane.data2[count]; // ��u�N���X�R�[�h
        return data2;
    }
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JComboBox jComboBox1 = new JComboBox();
    JPanel jPanel2 = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel jPanel3 = new JPanel();
    JPanel jPanel4 = new JPanel();
    JLabel jLabel1 = new JLabel();
    JButton jButton1 = new JButton();
    JButton jButton2 = new JButton();
    JPanel jPanel5 = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JLabel jLabel2 = new JLabel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabel3 = new JLabel();
    JLabel jLabel4 = new JLabel();
    KNJB033F1_Panel1 f3pane = new KNJB033F1_Panel1("(F)�R�s�[���E�����X�g");
    KNJB033F1_Panel2 f3pane2 = new KNJB033F1_Panel2("(T)�R�s�[��E�����X�g");
    KNJB033_Model_1 f2model1 = new KNJB033_Model_1();

    GridBagLayout gridBagLayout2 = new GridBagLayout();
    public KNJB033F1_Dlg(Frame frame, String title, boolean modal) {
      super(frame, title, modal);
      try
      {
        jbInit();
        pack();
      }
      catch(Exception ex)
      {
        ex.printStackTrace();
      }
    }

    public KNJB033F1_Dlg()
    {
        this(null, "", false);
    }
    void jbInit() throws Exception
    {



        f3pane.data1 = new String[50];
        f3pane.data2 = new String[50];
        f3pane.data3 = new String[50];
        f3pane2.data4 = new String[50];
        f3pane2.data5 = new int[50];
        panel1.setLayout(borderLayout1);
        jPanel1.setPreferredSize(new Dimension(10, 50));
        jPanel1.setLayout(borderLayout2);
        jPanel2.setPreferredSize(new Dimension(10, 70));
        jPanel2.setLayout(borderLayout3);
        jLabel1.setText("���E�̐E�����X�g���㉺�{�^���őΉ�����");
        jButton1.setMaximumSize(new Dimension(87, 35));
        jButton1.setMinimumSize(new Dimension(87, 35));
        jButton1.setPreferredSize(new Dimension(87, 35));
        jButton1.setText("�R�s�[(C)");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                jButton1_actionPerformed(e);
            }
        });
        jButton2.setMaximumSize(new Dimension(109, 35));
        jButton2.setMinimumSize(new Dimension(109, 35));
        jButton2.setPreferredSize(new Dimension(109, 35));
        jButton2.setText("�L�����Z��(X)");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton2_actionPerformed(e);
            }
        });
        jPanel5.setLayout(borderLayout4);
        jLabel2.setFont(new java.awt.Font("Dialog", 1, 30));
        jLabel2.setText("��");
        jPanel3.setLayout(gridBagLayout1);
        jLabel3.setText("�E�����ɕ��בւ��ĉ������B");
        jLabel4.setText("���j��ꂽ�E���͊���U���܂���B");
        jPanel4.setLayout(gridBagLayout2);

        jComboBox1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jComboBox1_actionPerformed(e);
            }
        });
        this.setModal(true);
        this.setTitle("����̃R�s�[");
        getContentPane().add(panel1);
        panel1.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(jComboBox1, BorderLayout.CENTER);
        jPanel1.setBorder(new TitledBorder("�����p�^�[��(�N���X�\��)�̑I���Ȗڃ��X�g"));
        panel1.add(jPanel2, BorderLayout.SOUTH);
        jPanel2.add(jPanel3, BorderLayout.WEST);
        jPanel3.add(jLabel1,     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 5, 0, 5), 0, 0));
        jPanel3.add(jLabel3,                  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 9, 0), 71, -2));
        jPanel3.add(jLabel4,     new GridBagConstraints(0, 2, 1, 2, 0.0, 0.0
                ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 23, 3));
        jPanel2.add(jPanel4, BorderLayout.EAST);
        jPanel4.add(jButton1,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(19, 6, 16, 0), 10, 0));
        jPanel4.add(jButton2,      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(19, 0, 16, 0), 17, 0));

// tajimaAdd 12/16
        /** �f�[�^�x�[�X�ɐڑ� */
/*
		String dbname = "//192.168.10.170/gakumudb";	// �c�a��
		String dbuser = "db2inst1";                     // �c�a���[�U��
		String dbpass = "db2inst1";                     // �c�a�p�X���[�h

        db2 = new DB2UDB(dbname, dbuser, dbpass, DB2UDB.TYPE3);
		try
        {
			db2.open();
		}
        catch( Exception ex )
        {
            System.out.println("�f�[�^�x�[�X�ڑ��G���[");

			return;	// �ȍ~�A�������Ȃ�
        }
*/
// tajimaAddEnd 12/16

        //���X�g�Ƀf�[�^��\��
        jPanel5.add(f3pane,BorderLayout.WEST);
        jPanel5.add(f3pane2,BorderLayout.EAST);

        panel1.add(jPanel5, BorderLayout.CENTER);
        jPanel5.add(jLabel2, BorderLayout.CENTER);
  }

    /**
     * DB�ڑ��ݒ�
	 * @param�@DB2UDB
    */
    public void setDB(DB2UDB db)
    {
        db2 = db;
    }

    /**
     * �R�s�[�{�^����������
	 * @param�@ActionEvent e
    */
    void jButton1_actionPerformed(ActionEvent e)
    {
        // �t���O�ݒ�
        chkflg = true;
        /** ��\�� */
        this.setVisible(false);
        /** Dialog�j�� */
        this.dispose();
    }

    /**
     * �R���{�{�b�N�X�I������
	 * @param�@ActionEvent e
    */
    void jComboBox1_actionPerformed(ActionEvent e)
    {
        int Cselect = 0,grpseq=0;
        String grpcd="0000";
        // �R���{�{�b�N�X���I�����ꂽ�s�ԍ����擾
        Cselect = jComboBox1.getSelectedIndex();
		if (Cselect <= 0)
		{
			return;
		}

        // Vector���f�[�^�擾
        Dataclass rowData = (Dataclass)data.elementAt(Cselect-1);
        grpcd = rowData.getgroupcd();  // �S�R�[�h
System.out.println("�I���s = "+ Cselect);
System.out.println("�Q�R�[�h= " + grpcd);
		// �I���Q�R�[�h�ݒ�
		SelGrpCD = grpcd;

        String lname;                  // �E���c��
        String fname;                  // �E����
        String subname;                // �Ȗږ�
        String attendcd2;              // ��u�N���X�R�[�h
        String staffcd2;               // �E���R�[�h
        String target2;                // �ΏۃN���X

        int k=0,lng=0,c=0;

        // �R�s�[���E�����X�g�\�����e�̕ύX
        /** SQL���̍쐬 */if(flag==1){
        String sql3 = "select "
                    + "T1.SUBCLASSCD, T2.SUBCLASSNAME, "
                    + "T1.STAFFCD, T3.LNAME_SHOW, "
                    + "T3.FNAME_SHOW ,T1.ATTENDCLASSCD, "
                    + "T4.CLASSALIAS ,T5.TARGETCLASS "
                    + "from "
                    + "electclassstaff_dat T1, v_subclass_mst T2, "
                    + "staff_mst T3, attendclass_hdat T4, attendclasscd_cre T5 "
                    + "where "
                    + "T1.YEAR='" + year +"' and T5.YEAR='" + year +"'"
                    + "and T1.GROUPCD=" + grpcd + " "
                    + "and T1.GROUP_SEQ=" + grpseq + " "
                    + "and T2.SUBCLASSYEAR='" + year +"' "
                    + "and T1.SUBCLASSCD=T2.SUBCLASSCD "
                    + "and T1.STAFFCD=T5.STAFFCD "
                    + "and T1.STAFFCD=T3.STAFFCD "
                    + "and T4.YEAR='" + year +"' "
                    + "and T1.ATTENDCLASSCD=T4.ATTENDCLASSCD "
                    + "and T1.ATTENDCLASSCD=T5.ATTENDCLASSCD "
                    + "order by 1,3";
System.out.println("F3Dialog ���sSQL = " + sql3);
        try
        {
            /** SQL���̎��s */
            db2.query(sql3);

            /** SQL���s���ʎ擾 */
            SqlResult = db2.getResultSet();

            // �\���p�f�[�^�N���A
            while(c<50)
            {
                f3pane.data1[c] = "";
                c++;
            }

            while( SqlResult.next() )
            {

                Dataclass d_class2 = new Dataclass();
                lname = SqlResult.getString("LNAME_SHOW");
                fname = SqlResult.getString("FNAME_SHOW");
                subcode2 = SqlResult.getString("SUBCLASSCD");
                attendcd2 = SqlResult.getString("ATTENDCLASSCD");
                staffcd2 = SqlResult.getString("STAFFCD");
                target2 = SqlResult.getString("TARGETCLASS");
                TaiClass = SqlResult.getString("TARGETCLASS");

                // �\���p�f�[�^�i�[
                f3pane.data1[k] = lname + "�@" + fname +
                "�@�@" + SqlResult.getString("SUBCLASSNAME");
                // ��u�N���X�R�[�h�i�[
                f3pane.data2[k] = attendcd2;
                // �E���R�[�h�i�[
                f3pane.data3[k] = staffcd2;
                k++;
                f3pane.jList1.setListData(f3pane.data1);
            }
            /** COMMIT */
            db2.commit();
        }
        catch ( Exception ex)
        {
            /** �f�[�^�擾�G���[ */
            System.out.println("�ŏI�G���[");
        }}
    }

    /**
     * �L�����Z���{�^����������
	 * @param�@ActionEvent e
    */
    void jButton2_actionPerformed(ActionEvent e)
    {
        chkflg = false;
        dispose();
    }

    /**
     * �R���{�{�b�N�X�\������
	 * @param�@DB2UDB
    */
    public void query(DB2UDB db2)
    {
        String StrDate1,StrDate2,StrDate3;  /* �J�n���t�ҏW�p */
        String EndDate1,EndDate2,EndDate3;  /* �I�����t�ҏW�p */
        String StrNendo;    /* �N�x�i�[�ϐ� */
        String StrGrpcd;    /* �Q�R�[�h */
        String StrSdate;    /* �J�n���t */
        String StrEdate;    /* �I�����t */
        String StrClass;    /* �ΏۃN���X */
        String StrName;     /* �I���Ȗږ��� */
        int j=0;
        int gcd=0,gcd4=0;
        String test;

//		jComboBox1.removeAllItems();
        /** �N�x�R���{�{�b�N�X�\���f�[�^�擾 */
        /** SQL���̍쐬 */
//        String sql = "select "
//                    + "T1.GROUPCD, "
//                    + "T2.GROUPNAME, "
//                    + "T1.GROUP_SEQ, "
//                    + "T1.STARTDATE, "
//                    + "T1.FINISHDATE "
//                    + "from electclassterm_dat T1, "
//                    + "electclass_mst T2,attendclasscd_cre T3 "
//                    + "where T1.YEAR= '" + year  + "' and T3.TARGETCLASS = '" + TaiClass + "' "
//                    + "and T1.GROUPCD = T2.GROUPCD and T1.YEAR = T3.YEAR "
//                    + "and T1.GROUPCD = T3.GROUPCD "
//                    + "order by 1, 3";

        String sql = "select "
                    + "T1.GROUPCD, "
                    + "T2.GROUPNAME, "
                    + "T1.GROUP_SEQ, "
                    + "T1.STARTDATE, "
                    + "T1.FINISHDATE, "
                    + "T1.CLASS "
                    + "from electclassterm_dat T1, "
                    + "electclass_mst T2 "
                    + "where T1.YEAR= '" + year + "' "
                    + "and T1.GROUPCD = T2.GROUPCD "
                    + "and T1.CLASS = '" + TaiClass +"' "
                    + "order by 1, 3";
System.out.println("�R�s�[�R���{�擾SQL=" + sql);
        try
        {
            /** SQL���̎��s */
            db2.query(sql);
            /** SQL���s���ʎ擾 */
            SqlResult = db2.getResultSet();

			data.removeAllElements();
//			jComboBox1.removeAllItems();

            while( SqlResult.next() )
            {
                Dataclass d_class = new Dataclass();
                StrGrpcd = SqlResult.getString("GROUPCD");
                StrSdate = SqlResult.getString("STARTDATE");
                StrEdate = SqlResult.getString("FINISHDATE");
                StrName = SqlResult.getString("GROUPNAME");

//                Dataclass Dclass = new Dataclass();
//                Dclass.setgroupcd(IntGrpcd);
//                Dclass.setseq(IntGrpseq);
//                data.add(Dclass);

                d_class.setgroupcd(StrGrpcd);
                data.addElement(d_class);

                /** �J�n���t�ҏW */
                StrDate1 = StrSdate.substring(0,4);
                StrDate2 = StrSdate.substring(5,7);
                StrDate3 = StrSdate.substring(8,10);
                /* �I�����t�ҏW */
                EndDate1 = StrEdate.substring(0,4);
                EndDate2 = StrEdate.substring(5,7);
                EndDate3 = StrEdate.substring(8,10);

System.out.println("�f�[�^�ǉ�");
                //** �Ώ۔N�x�w��R���{�{�b�N�X�ɒl��ݒ肷�� */
                /** �f�[�^�ǉ� */
                jComboBox1.addItem("�I���Ȗځm" + StrName + "�n");


            }
            flag=1;
            /** COMMIT */
            db2.commit();
        }
        catch (Exception e)
        {
            /** �N�x�f�[�^�擾�G���[ */
            System.out.println("�N�x�f�[�^�擾�G���[");
        }

    }

    /**
     * �R�s�[���E�����X�g�\������
	 * @param�@DB2UDB
    */
    public void query2(DB2UDB db2){
        String staffcd;  // �E���R�[�h
        String lname;    // �E���c��
        String fname;    // �E����
        String subname;  // �Ȗږ�
        String attendcd; // ��u�N���X�R�[�h
        String target;   // �ΏۃN���X

        int i=0;
      /** SQL���̍쐬 */
        String sql2 = "select "
                   + "T1.SUBCLASSCD, T2.SUBCLASSNAME, "
                   + "T1.STAFFCD, T3.LNAME_SHOW, "
                   + "T3.FNAME_SHOW ,T1.ATTENDCLASSCD, "
                   + "T4.CLASSALIAS ,T5.TARGETCLASS "
                   + "from "
                   + "electclassstaff_dat T1, v_subclass_mst T2, "
                   + "staff_mst T3, attendclass_hdat T4, attendclasscd_cre T5 "
                   + "where "
                   + "T1.YEAR='" + year +"' and T5.YEAR='" + year +"' "
                   + "and T1.GROUPCD= " + groupcd +" "
                   + "and T2.SUBCLASSYEAR='" + year +"' "
                   + "and T1.SUBCLASSCD=T2.SUBCLASSCD "
                   + "and T1.STAFFCD=T5.STAFFCD "
                   + "and T1.STAFFCD=T3.STAFFCD "
                   + "and T4.YEAR='" + year +"' "
                   + "and T1.ATTENDCLASSCD=T4.ATTENDCLASSCD "
                   + "and T1.ATTENDCLASSCD=T5.ATTENDCLASSCD "
                   + "order by 1,3";
        try
        {
            /** SQL���̎��s */
            db2.query(sql2);

            /** SQL���s���ʎ擾 */
            SqlResult = db2.getResultSet();

            while( SqlResult.next() )
            {
                // ���s���ʂ��f�[�^���擾
                lname = SqlResult.getString("LNAME_SHOW");
                fname = SqlResult.getString("FNAME_SHOW");
                subname = SqlResult.getString("SUBCLASSNAME");
                subcode = SqlResult.getString("SUBCLASSCD");
                attendcd = SqlResult.getString("ATTENDCLASSCD");
                TaiClass = SqlResult.getString("TARGETCLASS");
                staffcd = SqlResult.getString("STAFFCD");

                // �\���p�f�[�^�i�[
                f3pane.data1[i] = lname + "�@" + fname +"�@" + subname;
                // ��u�N���X�R�[�h�i�[
                f3pane.data2[i] = attendcd;
                // �E���R�[�h�i�[
                f3pane.data3[i] = staffcd;
                i++;
            }

            /** COMMIT */
            db2.commit();
        }
        catch ( Exception e)
        {
            /** �f�[�^�擾�G���[ */
            System.out.println("�R�s�[���E�����X�g ��u�N���X����f�[�^�擾�G���[");
        }
       f3pane.jList1.setListData(f3pane.data1);
    }

    /**
     * �R�s�[��E�����X�g�̃f�[�^���i�[����
	 * @param�@int
	 * @param�@String
    */
    public void set_data4(int i,String value)
    {
        f3pane2.data4[i] = value;
        f3pane2.data5[i] = i;
    }

    /**
     * �R�s�[��E�����X�g�\������
	 * @param�@����
    */
    public void SetRightPanel()
    {
        f3pane2.jList1.setListData(f3pane2.data4);
    }

    /**
     * �I���t���O�擾����
	 * @param�@����
    */
    public boolean getcheckflg()
    {
        System.out.println("checkflag is " + chkflg);
        return chkflg;
    }

    void jComboBox1_mouseClicked(MouseEvent e)
    {
    }

    /**
     * �f�[�^�N���A����
	 * @param�@����
    */
    public void ClearData()
    {
		jComboBox1.removeAllItems();
		jComboBox1.addItem("");

        for (int roopCnt = 0; roopCnt < 50; roopCnt++)
        {
            f3pane.data1[roopCnt] = "";
            f3pane.data2[roopCnt] = "";
            f3pane.data3[roopCnt] = "";
            f3pane2.data4[roopCnt] = "";
            f3pane2.data5[roopCnt] = 0;
        }

    }

}
