// kanji=����
/*
 * $Id: KNJB033.java,v 1.1 2004/06/03 06:54:52 tamura Exp $
 *
 * �쐬��: 2004/06/03 15:22:29 - JST
 * �쐬��: teruya
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import java.sql.ResultSet;

import jp.co.alp.kenja.hiro.knjb033.KNJB033_Dlg;



import nao_package.CalendarFrame;
import nao_package.db.DB2UDB;

/**
 * �I���Ȗڎ��{���ԓo�^(KNJB033)
 * @author      M.Miyagi
 * @version $Id: F1.java,v 1.21 2002/11/05 14:01:55 Miyagi Exp $
 */
public class KNJB033 extends JApplet {
    private KNJB033_Dlg          _form1;
    /** Database�N���X���p�������N���X */
    DB2UDB      db2;
    /** �c�a�̌������� */
    ResultSet   SqlResult;
    /** SQL���쐬�p */
    String      sql;
    /** ���ݔN�x */
    String      NowYear;
    /** �I��N�x */       // 2004-05-08 insert by teruya
    String      SelYear;  // 2004-05-08 insert by teruya
    /** �X�V���� */
    int         UpdateCnt;
    /** �폜���� */
    int         DeleteCnt;
    /** �u�J�����_�[�v�_�C�A���O */
    CalendarFrame calendarFrame;
    /** ���ݓ��t */
    java.util.Date NowTime;

    boolean isStandalone = false;
    JPanel bottom_panel = new JPanel();
    JScrollPane main_panel = new JScrollPane();
    JTable jTable1 = new JTable();
    JPanel jPanel2 = new JPanel();
    JButton btn_input = new JButton();
    JPanel jPanel3 = new JPanel();
    JButton btn_exit = new JButton();
    JButton btn_cancel = new JButton();
    JButton btn_save = new JButton();
    JPanel jPanel1 = new JPanel();
    JPanel head_panel = new JPanel();
    JButton btn_confirm = new JButton();
    JLabel nend_label = new JLabel();
    JComboBox nend_cmb = new JComboBox();
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenu1 = new JMenu();
    JMenu jMenu2 = new JMenu();
    JMenu jMenu3 = new JMenu();
    JMenuItem jMenuItem1 = new JMenuItem();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPanel4 = new JPanel();
    JTextArea jTextArea1 = new JTextArea();
    JComboBox jComboBox1 = new JComboBox();
    JButton jButton1 = new JButton();
    JTextArea jTextArea2 = new JTextArea();
    JButton jButton2 = new JButton();
    JTextArea jTextArea3 = new JTextArea();
    JLabel jLabel3 = new JLabel();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JLabel jLabel6 = new JLabel();
    JTextArea jTextArea4 = new JTextArea();
    JLabel jLabel8 = new JLabel();
    JButton jButton3 = new JButton();

    KNJB033_Dlg KNJB033_Dlg1 = new KNJB033_Dlg();
    JMenuItem jMenuItem3 = new JMenuItem();
    JMenuItem jMenuItem4 = new JMenuItem();
    JMenuItem jMenuItem6 = new JMenuItem();
    JMenuItem jMenuItem7 = new JMenuItem();
    JMenuItem jMenuItem10 = new JMenuItem();
    JButton btn_Update = new JButton();
    JButton btn_clear = new JButton();
    JButton btn_Insert = new JButton();
    JPanel statusPanel = new JPanel();
    JTextArea jTextArea5 = new JTextArea();
    JTextArea jTextArea6 = new JTextArea();
    JButton btn_Delete = new JButton();
    JTextArea jTextArea7 = new JTextArea();

    /**
     * �����l�̎擾
     * @param   String      key
     * @param   String      def
     */
    public String getParameter(String key, String def) {
        return isStandalone ? System.getProperty(key, def) :
          (getParameter(key) != null ? getParameter(key) : def);
    }

    /**
     * �A�v���b�g�̍\�z
     * @param   ����
     */
    public KNJB033() {
    }


    /**
     * destroy����
     * @param   ����
     * @see java.applet.Applet#destroy()
     */
    public void destroy() {
        super.destroy();
        System.out.println("destroy");
    }

    /**
     * @see java.applet.Applet#init()
     * @param   ����
     */
    public void init() {
        
        /** �f�[�^�x�[�X�ɐڑ� */
        String host   = getParameter("host");             // �z�X�g��
        String dbname = getParameter("dbname");           // �c�a��
        String dbuser = getParameter("dbuser");           // �c�a���[�U��
        String dbpass = getParameter("dbpass");           // �c�a�p�X���[�h
        String StrYear = getParameter("year");            // �N�x
        String StrSemester = getParameter("semester");    // �w��
        String StrChrCD = getParameter("chaircd");        // �u���R�[�h
        String StrGrpCD = getParameter("groupcd");        // �Q�R�[�h
        String StrGrpName = getParameter("groupname");    // �Q����
        String StrStaff = getParameter("staffcd");          // �E���R�[�h
        if (null == host)    { host = "192.168.50.125"; }
        if (null == dbname)  { dbname = "demodb"; }
        if (null == dbuser)  { dbuser = "db2inst1"; }
        if (null == dbpass)  { dbpass = "db2inst1"; }
        if (null == StrYear) { StrYear = "2002"; }
        if (null == StrSemester) { StrSemester = "2"; }
        if (null == StrChrCD) { StrChrCD = "0000"; }
        if (null == StrGrpCD) { StrGrpCD = "2101"; }
        if (null == StrGrpName) { StrGrpName = "�e�X�g�Q"; }
        dbname = "//" + host + "/" + dbname;

        db2 = new DB2UDB(dbname, dbuser, dbpass, DB2UDB.TYPE3);
        try
        {
            db2.open();
        }
        catch( Exception ex )
        {
            System.out.println("�f�[�^�x�[�X�ڑ��G���[");
            return; // �ȍ~�A�������Ȃ�
        }


        /** �I�����ݒ� */
        _form1 = new KNJB033_Dlg(getContentPane(), this, "", false,StrYear,StrSemester,StrChrCD,StrGrpCD,StrGrpName,StrStaff,db2);
        //KNJB033_Dlg1.query();
        /** ������͉�ʕ\�� */
//        KNJB033_Dlg1.show() ;
    }

    /**
     * �o�[�W������񏈗�
     * @param ����
     */
    void version_action() {
        JOptionPane.showMessageDialog(null, "�I���Ȗڎ��{���ԓo�^�@Ver1.0", "TITLE",
                              JOptionPane.INFORMATION_MESSAGE);
    }

   /**
     * �o�[�W������񏈗�(���j���[)
     * @param ActionEvent e
     */
    void jMenuItem10_actionPerformed(ActionEvent e) {
        version_action();
    }
}

// KNJB033

// eof