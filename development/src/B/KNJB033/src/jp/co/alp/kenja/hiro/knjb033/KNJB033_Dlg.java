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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractListModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.sql.ResultSet;

import nao_package.db.DB2UDB;

import jp.gr.java_conf.tame.swing.table.ColumnGroup;
import jp.gr.java_conf.tame.swing.table.GroupableTableHeader;
import jp.gr.java_conf.tame.swing.table.RowHeaderRenderer;

/**
 * ������͉��(LHA020F2)
 * @author m.miyagi
 * @version 1.0
 */

public class KNJB033_Dlg {
    /** �e��JApplet. �t���[���Ȃ�null. */
    protected               JApplet             _japplet = null;
	DB2UDB      db2;        // Database�N���X���p�������N���X
	ResultSet   SqlResult;  // �c�a�̌�������
    static String SelStaff;
    /** �R���e���c�E�y�C�� */
    protected               Container           _contentPane;
    private String SelYear; // �I��N�x
    private String SelSemester; // �w��
    private String SelChrCD;   // �u���R�[�h
    private String SelGrpCD;   // �Q�R�[�h
    private String SelGrpName; // �Q����
    private String SelClass;   // �ΏۃN���X
    int ColCnt;             // jTable2��
    int ColCnt2;            // jTable1��
    int length;
    String[] data2 = new String[20]; // ��u�N���X�R�[�h
    int[] data5 = new int[20]; // ��ԍ��ێ��p
    ResultSet SqlResult3;  // �c�a��������
    String popupmenu[];
    MenuItem menuItem[]; // jTable2�|�b�v�A�b�v���j���[����
    PopupMenu popup = new PopupMenu();  // jTable2�|�b�v�A�b�v���j���[ popup�̍쐬
    KNJB033F1_Panel1 f3pane1 = new KNJB033F1_Panel1();
    String popupmenu2;
    MenuItem menuItem2; // jTable1�|�b�v�A�b�v���j���[����
    PopupMenu popup2 = new PopupMenu(); // jTable1�|�b�v�A�b�v���j���[ popup2�̍쐬
    KNJB033F1_Dlg KNJB033F1_Dlg1 = new KNJB033F1_Dlg();
    boolean checkflg = false;
    String[] HdAdd_dat;
    String[] HdAdd_dat2;
    JLabel l1 = new JLabel();
    DefaultTableModel F2_Model_1 = new KNJB033_Model_1();
    DefaultTableModel F2_Model_2 = new KNJB033_Model_2();
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JTextField jTextField1 = new JTextField();
    JPanel jPanel1 = new JPanel();
    JSplitPane jSplitPane1 = new JSplitPane();
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane2 = new JScrollPane();
    JTable jTable1 = new JTable(){
        protected JTableHeader createDefaultTableHeader() {
	        return new GroupableTableHeader(columnModel);
        }
    };
    JTable jTable2 = new JTable();

    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPanel2 = new JPanel();
    JPanel jPanel3 = new JPanel();
    JButton jButton1 = new JButton();
    JButton jButton2 = new JButton();
    JButton jButton3 = new JButton();
    JButton jButton4 = new JButton();
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenu1 = new JMenu();
    JMenu jMenu2 = new JMenu();
    JMenu jMenu3 = new JMenu();
    JMenuItem jMenuItem1 = new JMenuItem();


    /**
     * �R���X�g���N�^
     */
    public KNJB033_Dlg(Container frame, Container parent, String title, boolean modal,String Year,String Semester,String ChrCD,String GrpCD,String GrpName,String Staff,DB2UDB paradb2) {
//        super(frame, title, modal);
        _contentPane = frame;
        _japplet = (JApplet)parent;
        SelStaff = Staff;
        try
        {
            setSelData(Year,Semester,ChrCD,GrpCD,GrpName,paradb2);
            jbInit();
            query();
//            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * �R���X�g���N�^
	 * @param	����
     */
    public KNJB033_Dlg()
    {
//        this(null, "", false);
    }


    void jbInit() throws Exception
    {
        panel1.setLayout(borderLayout1);
        jTextField1.setEditable(false);
//        jTextField1.setText("jTextField1");
        jTextField1.setText(SelGrpName);
        jPanel1.setPreferredSize(new Dimension(10, 40));
        jPanel1.setLayout(borderLayout2);
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setBottomComponent(null);
        jButton1.setPreferredSize(new Dimension(79, 25));
        jButton1.setMnemonic('L');
        jButton1.setText("�����z�u(L)");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton1_actionPerformed(e);
            }
        });
        jButton2.setPreferredSize(new Dimension(79, 25));
        jButton2.setMnemonic('S');
        jButton2.setText("�ۑ�(S)");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton2_actionPerformed(e);
            }
        });
        jButton3.setPreferredSize(new Dimension(79, 25));
        jButton3.setMnemonic('C');
        jButton3.setText("���(C)");
        jButton3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton3_actionPerformed(e);
            }
        });
        jButton4.setPreferredSize(new Dimension(79, 25));
        jButton4.setMnemonic('X');
        jButton4.setText("�I��(X)");
        jButton4.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                btn_exit_actionPerformed(e);
            }
        });
        jMenu1.setMnemonic('F');
        jMenu1.setText("�t�@�C��(F)");
        jMenu2.setMnemonic('V');
        jMenu2.setText("�\��(V)");
        jMenu3.setMnemonic('E');
        jMenu3.setText("�ҏW(E)");
        jMenuItem1.setText("����̃R�s�[");
        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(76, java.awt.event.KeyEvent.CTRL_MASK, false));
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItem1_actionPerformed(e);
            }
        });
        jMenuItem1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                jMenuItem1_mouseClicked(e);
            }
        });
//        _japplet.setJMenuBar(jMenuBar1);
//        _japplet.setName("�������");
        panel1.setMaximumSize(new Dimension(500, 500));
        panel1.setPreferredSize(new Dimension(456, 500));
        jTable2.setToolTipText("");
        jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable1.setCellSelectionEnabled(true);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                jTable1_mouseClicked(e);
            }
            public void mouseReleased(MouseEvent e)
            {
                Point p = e.getPoint();
                int row = jTable1.rowAtPoint(p);
                int column = jTable1.columnAtPoint(p);
                if(-1 != row && -1 != column)
                {
                    mouseReleasedAtCell_1(row,column,e);
                }
            }
        });
        // jTable2 �E�N���b�N����
        jTable2.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                jTable2_mouseClicked(e);
            }

            public void mouseReleased(MouseEvent e)
            {
                    Point   p = e.getPoint();
                    int row = jTable2.rowAtPoint(p);
                    int column = jTable2.columnAtPoint(p);
                    if(-1 != row && -1 != column)
                    {
                        mouseReleasedAtCell_2(row,column,e);
                    }
            }
        });
        jTable2.setCellSelectionEnabled(true);

        jScrollPane2.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                jScrollPane2_mouseClicked(e);
            }
        });
        popup.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                popup_actionPerformed(e);
            }
        });
        popup2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                popup2_actionPerformed(e);
            }
        });
        _contentPane.add(panel1);
        panel1.add(jTextField1, BorderLayout.NORTH);
        panel1.add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(jPanel2, BorderLayout.WEST);
        jPanel2.add(jButton1, null);
        jPanel1.add(jPanel3, BorderLayout.EAST);
        jPanel3.add(jButton2, null);
        jPanel3.add(jButton3, null);
        jPanel3.add(jButton4, null);
        panel1.add(jSplitPane1, BorderLayout.CENTER);
        jSplitPane1.add(jScrollPane2, JSplitPane.BOTTOM);
        jSplitPane1.add(jScrollPane1, JSplitPane.TOP);
        jScrollPane1.getViewport().add(jTable1, null);
        jScrollPane2.getViewport().add(jTable2, null);
        jMenuBar1.add(jMenu1);
        jMenuBar1.add(jMenu2);
        jMenuBar1.add(jMenu3);
        jMenu3.add(jMenuItem1);
        jSplitPane1.setDividerLocation(225);

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
    }

// tajimaAdd 12/16

    /**
     * DB�ڑ��ݒ�
	 * @param	DB2UDB
     */
    public void setDB(DB2UDB db)
    {
        db2 = db;
        KNJB033F1_Dlg1.setDB(db2);
    }
// tajimaAddEnd 12/16

    /**
     * ��ʕ\��
	 * @param	����
     */
    public void query()
    {
        ((KNJB033_Model_1)F2_Model_1).fireTableDataChanged();
        ((KNJB033_Model_2)F2_Model_2).fireTableDataChanged();
        /** �I�����ݒ� */
        ((KNJB033_Model_1)F2_Model_1).setSelData(SelYear, SelSemester, SelChrCD, SelGrpCD);
        ((KNJB033_Model_2)F2_Model_2).setSelData(SelYear, SelSemester, SelChrCD, SelGrpCD, SelStaff, SelClass);
        /** ��u�N���X����f�[�^�Z�b�g */
        ((KNJB033_Model_1)F2_Model_1).query(db2);
        /** �N���X����f�[�^�Z�b�g */
        ((KNJB033_Model_2)F2_Model_2).query(db2);
        /** ��u�N���X����f�[�^�\�� */
        jTable1.setModel(F2_Model_1);
        /** �N���X����f�[�^�\�� */
        jTable2.setModel(F2_Model_2);
        jTable2.setDefaultRenderer(Object.class,new MyCellRenderer());
        /** �e�[�u���\���ύX */
        ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        ((KNJB033_Model_2)F2_Model_2).fireTableStructureChanged();
        /** �񐔎擾 */
        ColCnt = ((KNJB033_Model_1)F2_Model_1).getColumnCount();

        HdAdd_dat = new String[ColCnt];
        HdAdd_dat2 = new String[ColCnt];
        TableColumn tc0 = null;
        /** �񐔕��\�� */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            HdAdd_dat[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd(cnt);
            HdAdd_dat2[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd2(cnt);
            /** �e�[�u���P�J�����T�C�Y�Z�b�g */
            tc0 = jTable1.getColumnModel().getColumn(cnt);
            tc0.setMinWidth(150);
            tc0.setMaxWidth(Integer.MAX_VALUE);
            tc0.setPreferredWidth(150);
        }

        //�w�b�_�̒ǉ�(�ȖځE�S�C������ю�u���k��)
        TableColumnModel cm = jTable1.getColumnModel();
        GroupableTableHeader header = (GroupableTableHeader)this.jTable1.getTableHeader();
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            ColumnGroup g_name = new ColumnGroup("<html>"+HdAdd_dat[cnt]+"<BR>"+HdAdd_dat2[cnt]);
            g_name.add(cm.getColumn(cnt));
            header.addColumnGroup(g_name);
        }

        int tbl2_ColCnt = ((KNJB033_Model_2)F2_Model_2).getColumnCount();
        TableColumn tc1;
        for (int roopCnt = 0; roopCnt < tbl2_ColCnt; roopCnt++)
        {
            /** �e�[�u���Q�J�����T�C�Y�Z�b�g */
            tc1 = jTable2.getColumnModel().getColumn(roopCnt);
            tc1.setMinWidth(150);
            tc1.setMaxWidth(Integer.MAX_VALUE);
            tc1.setPreferredWidth(150);
        }

        // jTable1�s�w�b�_�`��
        Table1Header();
        // jTable2�s�w�b�_�`��
        Table2Header();

        /**
         * jTable1�|�b�v�A�b�v���j���[�ǉ�
         */
        // �f�[�^������
        popup2.removeAll();
        menuItem2 = new MenuItem("���ɖ߂�");
        popup2.add(menuItem2);
        jTable1.add(popup2);

        // jTable2�|�b�v�A�b�v���j���[�ǉ�
        menuItem = new MenuItem[ColCnt];
        popupmenu = new String[ColCnt];
        // �f�[�^������
        popup.removeAll();
        for(int cnt=0;cnt<ColCnt;cnt++)
        {
            popupmenu[cnt] = KNJB033_Model_1.getColumnName2(cnt);
        }

        for(int cnt2=0;cnt2<ColCnt;cnt2++)
        {
            menuItem[cnt2] = new MenuItem(popupmenu[cnt2] + "�֓o�^");
            popup.add(menuItem[cnt2]);
        }
        jTable2.add(popup);
//        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * LHA020F2_Dlg�̃Z���E�����_��
     */
    class MyCellRenderer extends JLabel implements TableCellRenderer
    {
        public Component getTableCellRendererComponent
        (
            JTable table, Object data, boolean isSelected, boolean hasFocus,
            int row, int column)
            {
                setOpaque(true);	/* �w�i�F��񓧖��ɂ��� */
                JTableHeader header = table.getTableHeader();
                setForeground(Color.black);
                setBackground(Color.white);
                setFont(header.getFont());

                setHorizontalAlignment(JLabel.LEFT);
                setText((String)data);
                if (isSelected)
                {
                    /** �I��F�ݒ� */
                    setBackground(Color.blue);
                }


            return this;
        }
    }

    void jButton1_actionPerformed(ActionEvent e) {

    }

    void jMenuItem1_mouseClicked(MouseEvent e) {

    }

    /**
     * �R�s�[�{�^�� F3Dialog�\��
     */
    void jMenuItem1_actionPerformed(ActionEvent e)
    {
        /** F3��ʏ����� */
        KNJB033F1_Dlg1.ClearData();
        // KNJB033F1�̃C���X�^���X���쐬���e�ϐ��ɒl���i�[
        KNJB033F1_Dlg1.set_year(SelYear);
        KNJB033F1_Dlg1.set_groupcd(SelGrpCD);
        KNJB033F1_Dlg1.set_gname(SelGrpName);
        KNJB033F1_Dlg1.set_TaiClass(SelClass);
        ColCnt = ((KNJB033_Model_1)F2_Model_1).getColumnCount();
System.out.println("���� = " + ColCnt);
        // �R�s�[��E�����X�g�ɕ\������ׂ̐E�������i�[
        for(int i=0;i<ColCnt;i++)
        {
            KNJB033_Model_1.getColumnName2(i);
            KNJB033F1_Dlg1.set_data4(i,KNJB033_Model_1.getColumnName2(i));
        }
System.out.println("�E�����X�g�i�[");

        // �R���{�{�b�N�X�\���pSQL���s���\�b�h�Ăяo��
        ((KNJB033F1_Dlg)KNJB033F1_Dlg1).query(db2);
        // �R�s�[��E�����X�g�\���pSQL���s���\�b�h�Ăяo��
        ((KNJB033F1_Dlg)KNJB033F1_Dlg1).query2(db2);
        // �R�s�[��E�����X�g�\�����\�b�h�Ăяo��
        KNJB033F1_Dlg1.SetRightPanel();
        // KNJB033F1�_�C�A���O�Ăяo��
        KNJB033F1_Dlg1.show();
        // �I���t���O�擾
        // F3Dialog��ʂ́u�R�s�[�{�^���v�����̊m�F (true�͉���)
        checkflg = ((KNJB033F1_Dlg)KNJB033F1_Dlg1).getcheckflg();

        // �I���t���O����
        if(checkflg == true)
        {
            F3Returned();

        }
    }

    /**
     * �I���{�^��
     */
    void jButton4_actionPerformed(ActionEvent e)
    {
        /** ��\�� */
//        this.setVisible(false);
        /** Dialog�j�� */
//        this.dispose();
    }

    /**
     * �I������
     * @param ActionEvent e
     */
    void btn_exit_actionPerformed(ActionEvent e) {
        exit_action(e);
    }

    /**
     * �I������
     * @param ActionEvent e
     */
    void exit_action(ActionEvent e)
    {
        Exit.getInstance((JApplet)_japplet, _contentPane).actionPerformed(e);
    }

    /**
     * �I���f�[�^�ݒ�
	 * @param	�N�x
	 * @param	�Q�R�[�h
	 * @param	�I���Ȗږ���
	 * @param	�QSeq
	 * @param	�J�n���t
	 * @param	�I�����t
	 * @param	�ΏۃN���X
     */
    public void setSelData(String Year,String Semester,String ChrCD,String GrpCD,String GrpName,DB2UDB paradb2)
    {
        SelYear     =   Year;       // �I��N�x
        SelSemester =   Semester;   // �w��
        SelChrCD    =   ChrCD;      // �Q�R�[�h
        SelGrpCD    =   GrpCD;      // �Q�R�[�h
        SelGrpName  =   GrpName;    // �Q����
        db2         =   paradb2;    //
        SelClass    =   "";         // �ΏۃN���X
        /** �I�����\�� */
        jTextField1.setText("�I���Ȗ� " + SelGrpCD + "  " + SelGrpName );

        String sql;
        sql = "select distinct "
             + "T1.TRGTGRADE,"
             + "T1.TRGTCLASS, "
             + "CASE(T1.GROUPCD) "
             + "  WHEN '0000' THEN 'HR�u��' "
             + "              ELSE '�I���Q��:' || T2.GROUPNAME "
             + "END AS GROUPNAME "
             + "from "
             + "chair_cls_dat T1,"
             + "V_ELECTCLASS_MST T2 "
             + "where "
             + "T1.YEAR='" + SelYear + "' AND " 
             + "T1.SEMESTER='" + SelSemester + "' AND "; 
             if(SelGrpCD.equals("0000")) {
                sql += "    T1.GROUPCD='0000'  AND "
                    +  "    T1.CHAIRCD='" + SelChrCD + "'  AND ";
             }
             else {
                sql += "T1.GROUPCD='" + SelGrpCD + "'  AND "
                    +  "T1.GROUPCD=T2.GROUPCD AND ";
             }
             sql += "    T1.YEAR='" + Year + "' "
             + "order by T1.TRGTGRADE,T1.TRGTCLASS";

System.out.println("SQL = " + sql);
            try
            {
                /** SQL���̎��s */
                db2.query(sql);
                /** ���s���ʎ擾 */
                SqlResult = db2.getResultSet();
                while( SqlResult.next() )
                {
                    /** ���ݒ� */
                    SelClass    += SqlResult.getString("TRGTGRADE") + SqlResult.getString("TRGTCLASS");
                    SelGrpName  = SqlResult.getString("GROUPNAME");
                }
                /** COMMIT */
                db2.commit();

            }
            catch (Exception f)
            {
                System.out.println("���k���擾�G���[ = " + f.getMessage());
            }
    }

    void jScrollPane2_mouseClicked(MouseEvent e)
    {
    }



    void jTable2_mouseClicked(MouseEvent e)
    {

    }

    /**
    * jTable1 �E�N���b�N
    */
    void mouseReleasedAtCell_1(int row,int column,MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            System.out.println("jTable1 �E�N���b�N");
            popup2.show(e.getComponent(),e.getX(),e.getY());
        }
    }

    /**
    * jTable2 �E�N���b�N
    */
    void mouseReleasedAtCell_2(int row,int column,MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            System.out.println("�E�N���b�N");
            popup.show(e.getComponent(),e.getX(),e.getY());
        }
    }

    /**
     * jTable2�|�b�v�A�b�v���j���[�̃A�N�V����
     */
    void popup_actionPerformed(ActionEvent e)
    {

        String item = e.getActionCommand();
        int selectRow;
        int selectCol;
        int[] allselectRows = new int[jTable2.getSelectedRowCount()];
        int[] allselectCols = new int[jTable2.getSelectedColumnCount()];
        // �I���s�ԍ��擾
        allselectRows = jTable2.getSelectedRows();
        // �I���ԍ��擾
        allselectCols = jTable2.getSelectedColumns();

        int table1_Col = ((KNJB033_Model_1)F2_Model_1).getColumnCount();	// takaesu: ��̃e�[�u��(jTable1)�̗�

		// takaesu:��̃e�[�u���̊Y�������̌���
        for(int cnt = 0; cnt < table1_Col; cnt++)
        {
            System.out.println("���j���[�f�[�^" + menuItem[cnt].getLabel());
            if(menuItem[cnt].getLabel() == item)
            {
                System.out.println("���j���[�I��ԍ� " + cnt);	// takaesu: �|�b�v�A�b�v�̃A�C�e���̎w�W
                /** �I���s�E��擾 */
                int SelCol = jTable2.getSelectedColumn();
                int SelRow = jTable2.getSelectedRow();
for(int i=0; i<allselectRows.length; i++){	// takaesu: �I�����ꂽ�s���Ԃ�A��������B
                /** �ړ����k���擾�E�폜 */
                StudentData SelStudent = ((KNJB033_Model_2)F2_Model_2).getStudentData(SelRow,SelCol);
                /** �ړ����k���ݒ� */
                ((KNJB033_Model_1)F2_Model_1).setStudent(cnt,
                                                          SelStudent.getData(0),
                                                          SelStudent.getData(1),
                                                          SelStudent.getData(2),
                                                          SelStudent.getData(3),
//                                                          SelStudent.getData(4),
                                                          SelStudent.getData(5),
                                                          SelStudent.getData(9),
                                                          SelStudent.getData(10),
                                                          SelStudent.getData(7),
                                                          SelStudent.getData(8),
                                                          SelStudent.getData(11)
                                                          );

}
                /** �ő�s���X�V */
                int MaxCol = ((KNJB033_Model_1)F2_Model_1).getColumnCount();
                ((KNJB033_Model_1)F2_Model_1).getMaxRow(MaxCol);
                /** �e�[�u���ĕ\�� */
//                ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        		// jTable1 �s�w�b�_�ĕ`��
//        		Table1Header();
//                jTable2.repaint();
				ReDsp();
                break;
            }
        }

        /** �J�����T�C�Y�ݒ� */
        setColSize();
    }


    /**
     * jTable1�|�b�v�A�b�v���j���[�A�N�V����
     */
    void popup2_actionPerformed(ActionEvent e)
    {
        int MaxCol = ((KNJB033_Model_2)F2_Model_2).getColumnCount();
        String GradeClass; // jTable1�őI�����ꂽ�Z���� �w�N + �N���X
        String GradeClass2; // jTable2�ւ̈ړ��ꏊ�w��p �w�N + �N���X

        /** �I���s�E��擾 */
//        int SelCol2 = jTable1.getSelectedColumn();
//        int SelRow2 = jTable1.getSelectedRow();
        int SelCol2[] = jTable1.getSelectedColumns();
        int SelRow2[] = jTable1.getSelectedRows();
//        System.out.println(SelRow2 + "�s " + SelCol2 + "��");
for(int i = SelRow2.length; i > 0 ; i--) {
        /** �ړ����k���擾�E�폜 */
        StudentData SelStudent2 = ((KNJB033_Model_1)F2_Model_1).getStudentData(SelRow2[i-1],SelCol2[0]);
        GradeClass = SelStudent2.getData(0) + SelStudent2.getData(1);
        /** �ړ����k���ݒ� */
        for(int cnt = 0;cnt < MaxCol;cnt++)
        {
            GradeClass2 = ((KNJB033_Model_2)F2_Model_2).getColumnName(cnt).substring(0,2)
                        + ((KNJB033_Model_2)F2_Model_2).getColumnName(cnt).substring(3,5);
            if(GradeClass.equals(GradeClass2))
            {
                ((KNJB033_Model_2)F2_Model_2).setStudent(cnt,
                                                          SelStudent2.getData(0),
                                                          SelStudent2.getData(1),
                                                          SelStudent2.getData(2),
                                                          SelStudent2.getData(3),
//                                                          SelStudent2.getData(4),
                                                          SelStudent2.getData(5));
                /** �ő�s���X�V */

                int MaxCol2 = ((KNJB033_Model_2)F2_Model_2).getColumnCount();
                ((KNJB033_Model_2)F2_Model_2).getMaxRow(MaxCol2);

                /** �e�[�u���ĕ\�� */
                ((KNJB033_Model_2)F2_Model_2).fireTableStructureChanged();
                Table2Header();
                jTable1.repaint();

            }
			else
			{
				jTable1.repaint();
			}
        }
}

        /** �J�����T�C�Y�ݒ� */
        setColSize();
   }

    /**
     * F3Dlg����̖߂�
	 * @param	DB2UDB
     */
    public void query2(DB2UDB db2)
    {
        System.out.println("query2 �Ăяo���m�F");
        String attendclasscd;
        int targetCol;
        String sql2;
        int targetCnt = 0;
        ((KNJB033_Model_1)F2_Model_1).AllClear();
        for (int rpCnt = 0; rpCnt < length; rpCnt++)
        {
            try
            {
                /** ��u�N���X�R�[�h�ݒ� */
                attendclasscd = data2[rpCnt];
                System.out.println("��u�N���X�R�[�h�m�F " + data2[rpCnt] + "rpcnt is " + rpCnt);
                targetCnt = data5[rpCnt];
            }
            catch (Exception e)
            {
                break;
            }
       sql2 = "select "
                + "T1.SCHREGNO, "
                + "T2.GRADE, "
                + "T2.HR_CLASS, "
                + "T2.ATTENDNO, "
                + "T3.NAME_SHOW, "
                + "T1.ROW, "
                + "T1.COLUMN, "
                + "T1.APPDATE, "
                + "T1.APPENDDATE, "
                + "T1.REGISTERCD "
                + "from "
                + "CHAIR_STD_DAT T1, "
                + "schreg_regd_dat T2, "
                + "schreg_base_mst T3 "
                + "where "
                + "T1.YEAR='" + SelYear + "' "
                + "and T1.YEAR = T2.YEAR "
                + "and T1.SEMESTER = '" + SelSemester +"' "
                + "and T1.SEMESTER = T2.SEMESTER "
                + "and T1.CHAIRCD = '" + SelChrCD +"' "
                + "and T1.SCHREGNO = T2.SCHREGNO "
                + "and T2.SCHREGNO = T3.SCHREGNO "
                + "order by T2.GRADE,T2.HR_CLASS,T2.ATTENDNO";
System.out.println("SQL = " + sql2);
            try
            {
                /** SQL���̎��s */
                db2.query(sql2);
                /** ���s���ʎ擾 */
                SqlResult3 = db2.getResultSet();
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
                int rowcount = SqlResult3.getRow();

                while( SqlResult3.next() )
                {
                    /** ���ݒ� */
                    StrGrade    = SqlResult3.getString("GRADE");
                    StrClass    = SqlResult3.getString("HR_CLASS");
                    StrAttendNo = SqlResult3.getString("ATTENDNO");
                    StrName    = SqlResult3.getString("NAME_SHOW");
                    StrSchregno = SqlResult3.getString("SCHREGNO");
                    StrRow      = SqlResult3.getString("ROW");
                    StrColumn   = SqlResult3.getString("COLUMN");
                    StrAppDate  = SqlResult3.getString("APPDATE");
                    StrAppEndDate = SqlResult3.getString("APPENDDATE");
                    StrRegisterCD = SqlResult3.getString("REGISTERCD");

                    /** ���k���i�[ */
                    ((KNJB033_Model_1)F2_Model_1).setStudent(targetCnt,StrGrade,StrClass,
                    StrAttendNo,StrName,StrSchregno,StrRow,StrColumn,StrAppDate,StrAppEndDate,StrRegisterCD);
                }
                /** COMMIT */
                db2.commit();

            }
            catch (Exception f)
            {
                System.out.println("���k���擾�G���[ = " + f.getMessage());
                break;
            }
        }

        /** �ő�s���擾 */
        int MaxCol = ((KNJB033_Model_1)F2_Model_1).getColumnCount();
        /** �ő�s���X�V */
        ((KNJB033_Model_1)F2_Model_1).getMaxRow(MaxCol);

        /** �J�����T�C�Y�ݒ� */
        setColSize();
    }

    void jTable1_mouseClicked(MouseEvent e)
    {

    }

    /**
     * �ۑ��{�^��
     */
    void jButton2_actionPerformed(ActionEvent e)
    {
        int number=0;
        // jTable1�̗񐔎擾
        int MaxCol = ((KNJB033_Model_1)F2_Model_1).getColumnCount();
        /** �X�V�֐� */
        boolean flg = ((KNJB033_Model_1)F2_Model_1).setDB(db2);
        /** �߂�l���� */
        if (flg == true)
        {
            JOptionPane.showMessageDialog(null, "����ɏI�����܂���", "����",
                                  JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(null, "�ۑ��Ɏ��s���܂���", "����",
                                  JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * ����{�^��
     */
    void jButton3_actionPerformed(ActionEvent e)
    {
        // jTable1 �\�����e�N���A
        ((KNJB033_Model_1)F2_Model_1).AllClear();
        // jTable1 SQL���s
        ((KNJB033_Model_1)F2_Model_1).query(db2);
        // jTable1�ĕ`��
        ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        /** jTable1�̗񐔕��\��(�w�b�_) */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            HdAdd_dat[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd(cnt);
        }
        //�w�b�_�̒ǉ�(jTable1)
        TableColumnModel cm = jTable1.getColumnModel();
        GroupableTableHeader header = (GroupableTableHeader)this.jTable1.getTableHeader();
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            ColumnGroup g_name = new ColumnGroup(HdAdd_dat[cnt]);
            g_name.add(cm.getColumn(cnt));
            header.addColumnGroup(g_name);
        }

        // jTable2 �\���p�f�[�^�N���A
        ((KNJB033_Model_2)F2_Model_2).AllClear();
        // jTable2 SQL�p�ϐ��ݒ�
        ((KNJB033_Model_2)F2_Model_2).setSelData(SelYear, SelSemester, SelChrCD, SelGrpCD, SelStaff, SelClass);
        // jTable2 SQL���s
        ((KNJB033_Model_2)F2_Model_2).query(db2);
        // jTable2 �ĕ`��
        ((KNJB033_Model_2)F2_Model_2).fireTableStructureChanged();

        /** �J�����T�C�Y�ݒ� */
        setColSize();
    }

    /**
     * ����̃R�s�[����̖߂�(�e�[�u���̍ĕ`��)
	 * @param	����
     */
    public void F3Returned()
    {
	  if (KNJB033F1_Dlg1.getcheckflg())
	  {
        length = KNJB033F1_Dlg1.getColCnt();
        // ��u�N���X�R�[�h�擾
        for(int i=0;i<length;i++)
        {
            data2[i] = KNJB033F1_Dlg1.getData(1,i);
        }
        // �Ώۗ�ԍ��擾
        for(int j=0;j<length;j++)
        {
            data5[j] = KNJB033F1_Dlg1.gettargetColumn(j);
        }
        // jTable1 SQL���s
        query2(db2);
        // jTable1 �ĕ`��
        ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        // jTable1 �s�w�b�_�ĕ`��
        Table1Header();
        /** jTable1�̗񐔕��\��(�w�b�_) */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            HdAdd_dat[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd(cnt);
        }
        //�w�b�_�̒ǉ�(jTable1)
        TableColumnModel cm = jTable1.getColumnModel();
        GroupableTableHeader header = (GroupableTableHeader)this.jTable1.getTableHeader();
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            ColumnGroup g_name = new ColumnGroup(HdAdd_dat[cnt]);
            g_name.add(cm.getColumn(cnt));
            header.addColumnGroup(g_name);
        }

        String tClass = KNJB033F1_Dlg1.gettergetClass();
        String GrpCD = KNJB033F1_Dlg1.getGrpCD();
		// F3�I���Q�R�[�h�擾
		String F3GrpCD = KNJB033F1_Dlg1.F3GetGrpCD();
        // SQL�p�ϐ��ݒ�
        ((KNJB033_Model_2)F2_Model_2).setSelData(SelYear,SelSemester,SelChrCD,F3GrpCD,SelStaff,tClass);
        // SQL���s
        ((KNJB033_Model_2)F2_Model_2).query(db2);
        // jTable2 �ĕ`��
        ((KNJB033_Model_2)F2_Model_2).fireTableStructureChanged();
        // jTable2 �s�w�b�_�ĕ`��
        Table2Header();

        /** �J�����T�C�Y�ݒ� */
        setColSize();
	  }
    }

    /**
     * jTable1 �s�w�b�_�ǉ�
	 * @param	����
    */
     public void Table1Header()
     {
        ListModel lm = new AbstractListModel()
        {
            public int getSize()
            {
                return ((KNJB033_Model_1)F2_Model_1).getRowCount();
            }
            public Object getElementAt(int index)
            {
                return String.valueOf(index);
            }
        };
        JList rowHeader = new JList(lm);
        rowHeader.setFixedCellWidth(30);	// �s���o���́u���v
        rowHeader.setFixedCellHeight(jTable1.getRowHeight());
        rowHeader.setBackground(Color.lightGray);
        rowHeader.setCellRenderer(new RowHeaderRenderer(jTable1, SwingConstants.LEFT));
        // �s���o�����X�N���[���E�y�C���ɐݒ�
        jScrollPane1.setRowHeaderView(rowHeader);
    }

    /**
     * jTable2 �s�w�b�_�ǉ�
	 * @param	����
     */
     public void Table2Header()
     {
        // jTable2�s�w�b�_�[�ݒ�
        ListModel lm2 = new AbstractListModel()
        {
            public int getSize()
            {
                return ((KNJB033_Model_2)F2_Model_2).getRowCount();
            }
            public Object getElementAt(int index)
            {
                return String.valueOf(index+1);
            }
        };
        JList rowHeader2 = new JList(lm2);
        rowHeader2.setFixedCellWidth(30);	// �s���o���́u���v
        rowHeader2.setFixedCellHeight(jTable2.getRowHeight());
        rowHeader2.setBackground(Color.lightGray);
        rowHeader2.setCellRenderer(new RowHeaderRenderer(jTable2, SwingConstants.LEFT));
        // �s���o�����X�N���[���E�y�C���ɐݒ�
        jScrollPane2.setRowHeaderView(rowHeader2);
    }

    /**
     * �J�����T�C�Y�ݒ�
	 * @param	����
     */
    public void setColSize()
    {
        /** Table1 �T�C�Y�ݒ� */
        TableColumn tc0;
        /** �񐔕��\�� */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            /** �e�[�u���P�J�����T�C�Y�Z�b�g */
            tc0 = jTable1.getColumnModel().getColumn(cnt);
            tc0.setMinWidth(150);
            tc0.setMaxWidth(Integer.MAX_VALUE);
            tc0.setPreferredWidth(150);
        }
        /** Table2 �T�C�Y�ݒ� */
        int tbl2_ColCnt = ((KNJB033_Model_2)F2_Model_2).getColumnCount();
        TableColumn tc1;
        for (int roopCnt = 0; roopCnt < tbl2_ColCnt; roopCnt++)
        {
            /** �e�[�u���Q�J�����T�C�Y�Z�b�g */
            tc1 = jTable2.getColumnModel().getColumn(roopCnt);
            tc1.setMinWidth(150);
            tc1.setMaxWidth(Integer.MAX_VALUE);
            tc1.setPreferredWidth(150);
        }
    }


    /**
     * �ĕ`��
	 * @param	����
     */
    public void ReDsp()
    {
        // jTable1 �ĕ`��
        ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        // jTable1 �s�w�b�_�ĕ`��
        Table1Header();
        /** jTable1�̗񐔕��\��(�w�b�_) */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            HdAdd_dat[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd(cnt);
        }
        //�w�b�_�̒ǉ�(jTable1)
        TableColumnModel cm = jTable1.getColumnModel();
        GroupableTableHeader header = (GroupableTableHeader)this.jTable1.getTableHeader();
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            ColumnGroup g_name = new ColumnGroup(HdAdd_dat[cnt]);
            g_name.add(cm.getColumn(cnt));
            header.addColumnGroup(g_name);
        }

        String tClass = KNJB033F1_Dlg1.gettergetClass();
        String GrpCD = KNJB033F1_Dlg1.getGrpCD();
        // jTable2 �ĕ`��
        ((KNJB033_Model_2)F2_Model_2).fireTableStructureChanged();
        // jTable2 �s�w�b�_�ĕ`��
        Table2Header();
    }
}
