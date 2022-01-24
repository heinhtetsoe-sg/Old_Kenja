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
 * 名簿入力画面(LHA020F2)
 * @author m.miyagi
 * @version 1.0
 */

public class KNJB033_Dlg {
    /** 親のJApplet. フレームならnull. */
    protected               JApplet             _japplet = null;
	DB2UDB      db2;        // Databaseクラスを継承したクラス
	ResultSet   SqlResult;  // ＤＢの検索結果
    static String SelStaff;
    /** コンテンツ・ペイン */
    protected               Container           _contentPane;
    private String SelYear; // 選択年度
    private String SelSemester; // 学期
    private String SelChrCD;   // 講座コード
    private String SelGrpCD;   // 群コード
    private String SelGrpName; // 群名称
    private String SelClass;   // 対象クラス
    int ColCnt;             // jTable2列数
    int ColCnt2;            // jTable1列数
    int length;
    String[] data2 = new String[20]; // 受講クラスコード
    int[] data5 = new int[20]; // 列番号保持用
    ResultSet SqlResult3;  // ＤＢ検索結果
    String popupmenu[];
    MenuItem menuItem[]; // jTable2ポップアップメニュー項目
    PopupMenu popup = new PopupMenu();  // jTable2ポップアップメニュー popupの作成
    KNJB033F1_Panel1 f3pane1 = new KNJB033F1_Panel1();
    String popupmenu2;
    MenuItem menuItem2; // jTable1ポップアップメニュー項目
    PopupMenu popup2 = new PopupMenu(); // jTable1ポップアップメニュー popup2の作成
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
     * コンストラクタ
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
     * コンストラクタ
	 * @param	無し
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
        jButton1.setText("自動配置(L)");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton1_actionPerformed(e);
            }
        });
        jButton2.setPreferredSize(new Dimension(79, 25));
        jButton2.setMnemonic('S');
        jButton2.setText("保存(S)");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton2_actionPerformed(e);
            }
        });
        jButton3.setPreferredSize(new Dimension(79, 25));
        jButton3.setMnemonic('C');
        jButton3.setText("取消(C)");
        jButton3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton3_actionPerformed(e);
            }
        });
        jButton4.setPreferredSize(new Dimension(79, 25));
        jButton4.setMnemonic('X');
        jButton4.setText("終了(X)");
        jButton4.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                btn_exit_actionPerformed(e);
            }
        });
        jMenu1.setMnemonic('F');
        jMenu1.setText("ファイル(F)");
        jMenu2.setMnemonic('V');
        jMenu2.setText("表示(V)");
        jMenu3.setMnemonic('E');
        jMenu3.setText("編集(E)");
        jMenuItem1.setText("名簿のコピー");
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
//        _japplet.setName("名簿入力");
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
        // jTable2 右クリック処理
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
        /** データベースに接続 */
/*
        String dbname = "//192.168.10.170/gakumudb";	// ＤＢ名
        String dbuser = "db2inst1";                     // ＤＢユーザ名
        String dbpass = "db2inst1";                     // ＤＢパスワード

        db2 = new DB2UDB(dbname, dbuser, dbpass, DB2UDB.TYPE3);
        try
        {
            db2.open();
        }
        catch( Exception ex )
        {
            System.out.println("データベース接続エラー");
            return;	// 以降、処理しない
        }
*/
// tajimaAddEnd 12/16
    }

// tajimaAdd 12/16

    /**
     * DB接続設定
	 * @param	DB2UDB
     */
    public void setDB(DB2UDB db)
    {
        db2 = db;
        KNJB033F1_Dlg1.setDB(db2);
    }
// tajimaAddEnd 12/16

    /**
     * 画面表示
	 * @param	無し
     */
    public void query()
    {
        ((KNJB033_Model_1)F2_Model_1).fireTableDataChanged();
        ((KNJB033_Model_2)F2_Model_2).fireTableDataChanged();
        /** 選択情報設定 */
        ((KNJB033_Model_1)F2_Model_1).setSelData(SelYear, SelSemester, SelChrCD, SelGrpCD);
        ((KNJB033_Model_2)F2_Model_2).setSelData(SelYear, SelSemester, SelChrCD, SelGrpCD, SelStaff, SelClass);
        /** 受講クラス名簿データセット */
        ((KNJB033_Model_1)F2_Model_1).query(db2);
        /** クラス名簿データセット */
        ((KNJB033_Model_2)F2_Model_2).query(db2);
        /** 受講クラス名簿データ表示 */
        jTable1.setModel(F2_Model_1);
        /** クラス名簿データ表示 */
        jTable2.setModel(F2_Model_2);
        jTable2.setDefaultRenderer(Object.class,new MyCellRenderer());
        /** テーブル構造変更 */
        ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        ((KNJB033_Model_2)F2_Model_2).fireTableStructureChanged();
        /** 列数取得 */
        ColCnt = ((KNJB033_Model_1)F2_Model_1).getColumnCount();

        HdAdd_dat = new String[ColCnt];
        HdAdd_dat2 = new String[ColCnt];
        TableColumn tc0 = null;
        /** 列数分表示 */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            HdAdd_dat[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd(cnt);
            HdAdd_dat2[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd2(cnt);
            /** テーブル１カラムサイズセット */
            tc0 = jTable1.getColumnModel().getColumn(cnt);
            tc0.setMinWidth(150);
            tc0.setMaxWidth(Integer.MAX_VALUE);
            tc0.setPreferredWidth(150);
        }

        //ヘッダの追加(科目・担任名および受講生徒名)
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
            /** テーブル２カラムサイズセット */
            tc1 = jTable2.getColumnModel().getColumn(roopCnt);
            tc1.setMinWidth(150);
            tc1.setMaxWidth(Integer.MAX_VALUE);
            tc1.setPreferredWidth(150);
        }

        // jTable1行ヘッダ描画
        Table1Header();
        // jTable2行ヘッダ描画
        Table2Header();

        /**
         * jTable1ポップアップメニュー追加
         */
        // データ初期化
        popup2.removeAll();
        menuItem2 = new MenuItem("元に戻す");
        popup2.add(menuItem2);
        jTable1.add(popup2);

        // jTable2ポップアップメニュー追加
        menuItem = new MenuItem[ColCnt];
        popupmenu = new String[ColCnt];
        // データ初期化
        popup.removeAll();
        for(int cnt=0;cnt<ColCnt;cnt++)
        {
            popupmenu[cnt] = KNJB033_Model_1.getColumnName2(cnt);
        }

        for(int cnt2=0;cnt2<ColCnt;cnt2++)
        {
            menuItem[cnt2] = new MenuItem(popupmenu[cnt2] + "へ登録");
            popup.add(menuItem[cnt2]);
        }
        jTable2.add(popup);
//        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * LHA020F2_Dlgのセル・レンダラ
     */
    class MyCellRenderer extends JLabel implements TableCellRenderer
    {
        public Component getTableCellRendererComponent
        (
            JTable table, Object data, boolean isSelected, boolean hasFocus,
            int row, int column)
            {
                setOpaque(true);	/* 背景色を非透明にする */
                JTableHeader header = table.getTableHeader();
                setForeground(Color.black);
                setBackground(Color.white);
                setFont(header.getFont());

                setHorizontalAlignment(JLabel.LEFT);
                setText((String)data);
                if (isSelected)
                {
                    /** 選択色設定 */
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
     * コピーボタン F3Dialog表示
     */
    void jMenuItem1_actionPerformed(ActionEvent e)
    {
        /** F3画面初期化 */
        KNJB033F1_Dlg1.ClearData();
        // KNJB033F1のインスタンスを作成し各変数に値を格納
        KNJB033F1_Dlg1.set_year(SelYear);
        KNJB033F1_Dlg1.set_groupcd(SelGrpCD);
        KNJB033F1_Dlg1.set_gname(SelGrpName);
        KNJB033F1_Dlg1.set_TaiClass(SelClass);
        ColCnt = ((KNJB033_Model_1)F2_Model_1).getColumnCount();
System.out.println("件数 = " + ColCnt);
        // コピー先職員リストに表示する為の職員名を格納
        for(int i=0;i<ColCnt;i++)
        {
            KNJB033_Model_1.getColumnName2(i);
            KNJB033F1_Dlg1.set_data4(i,KNJB033_Model_1.getColumnName2(i));
        }
System.out.println("職員リスト格納");

        // コンボボックス表示用SQL実行メソッド呼び出し
        ((KNJB033F1_Dlg)KNJB033F1_Dlg1).query(db2);
        // コピー先職員リスト表示用SQL実行メソッド呼び出し
        ((KNJB033F1_Dlg)KNJB033F1_Dlg1).query2(db2);
        // コピー先職員リスト表示メソッド呼び出し
        KNJB033F1_Dlg1.SetRightPanel();
        // KNJB033F1ダイアログ呼び出し
        KNJB033F1_Dlg1.show();
        // 選択フラグ取得
        // F3Dialog画面の「コピーボタン」押下の確認 (trueは押下)
        checkflg = ((KNJB033F1_Dlg)KNJB033F1_Dlg1).getcheckflg();

        // 選択フラグ判定
        if(checkflg == true)
        {
            F3Returned();

        }
    }

    /**
     * 終了ボタン
     */
    void jButton4_actionPerformed(ActionEvent e)
    {
        /** 非表示 */
//        this.setVisible(false);
        /** Dialog破棄 */
//        this.dispose();
    }

    /**
     * 終了処理
     * @param ActionEvent e
     */
    void btn_exit_actionPerformed(ActionEvent e) {
        exit_action(e);
    }

    /**
     * 終了処理
     * @param ActionEvent e
     */
    void exit_action(ActionEvent e)
    {
        Exit.getInstance((JApplet)_japplet, _contentPane).actionPerformed(e);
    }

    /**
     * 選択データ設定
	 * @param	年度
	 * @param	群コード
	 * @param	選択科目名称
	 * @param	群Seq
	 * @param	開始日付
	 * @param	終了日付
	 * @param	対象クラス
     */
    public void setSelData(String Year,String Semester,String ChrCD,String GrpCD,String GrpName,DB2UDB paradb2)
    {
        SelYear     =   Year;       // 選択年度
        SelSemester =   Semester;   // 学期
        SelChrCD    =   ChrCD;      // 群コード
        SelGrpCD    =   GrpCD;      // 群コード
        SelGrpName  =   GrpName;    // 群名称
        db2         =   paradb2;    //
        SelClass    =   "";         // 対象クラス
        /** 選択情報表示 */
        jTextField1.setText("選択科目 " + SelGrpCD + "  " + SelGrpName );

        String sql;
        sql = "select distinct "
             + "T1.TRGTGRADE,"
             + "T1.TRGTCLASS, "
             + "CASE(T1.GROUPCD) "
             + "  WHEN '0000' THEN 'HR講座' "
             + "              ELSE '選択群名:' || T2.GROUPNAME "
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
                /** SQL文の実行 */
                db2.query(sql);
                /** 実行結果取得 */
                SqlResult = db2.getResultSet();
                while( SqlResult.next() )
                {
                    /** 情報設定 */
                    SelClass    += SqlResult.getString("TRGTGRADE") + SqlResult.getString("TRGTCLASS");
                    SelGrpName  = SqlResult.getString("GROUPNAME");
                }
                /** COMMIT */
                db2.commit();

            }
            catch (Exception f)
            {
                System.out.println("生徒情報取得エラー = " + f.getMessage());
            }
    }

    void jScrollPane2_mouseClicked(MouseEvent e)
    {
    }



    void jTable2_mouseClicked(MouseEvent e)
    {

    }

    /**
    * jTable1 右クリック
    */
    void mouseReleasedAtCell_1(int row,int column,MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            System.out.println("jTable1 右クリック");
            popup2.show(e.getComponent(),e.getX(),e.getY());
        }
    }

    /**
    * jTable2 右クリック
    */
    void mouseReleasedAtCell_2(int row,int column,MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            System.out.println("右クリック");
            popup.show(e.getComponent(),e.getX(),e.getY());
        }
    }

    /**
     * jTable2ポップアップメニューのアクション
     */
    void popup_actionPerformed(ActionEvent e)
    {

        String item = e.getActionCommand();
        int selectRow;
        int selectCol;
        int[] allselectRows = new int[jTable2.getSelectedRowCount()];
        int[] allselectCols = new int[jTable2.getSelectedColumnCount()];
        // 選択行番号取得
        allselectRows = jTable2.getSelectedRows();
        // 選択列番号取得
        allselectCols = jTable2.getSelectedColumns();

        int table1_Col = ((KNJB033_Model_1)F2_Model_1).getColumnCount();	// takaesu: 上のテーブル(jTable1)の列数

		// takaesu:上のテーブルの該当する列の検索
        for(int cnt = 0; cnt < table1_Col; cnt++)
        {
            System.out.println("メニューデータ" + menuItem[cnt].getLabel());
            if(menuItem[cnt].getLabel() == item)
            {
                System.out.println("メニュー選択番号 " + cnt);	// takaesu: ポップアップのアイテムの指標
                /** 選択行・列取得 */
                int SelCol = jTable2.getSelectedColumn();
                int SelRow = jTable2.getSelectedRow();
for(int i=0; i<allselectRows.length; i++){	// takaesu: 選択された行数ぶん、処理する。
                /** 移動生徒情報取得・削除 */
                StudentData SelStudent = ((KNJB033_Model_2)F2_Model_2).getStudentData(SelRow,SelCol);
                /** 移動生徒情報設定 */
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
                /** 最大行数更新 */
                int MaxCol = ((KNJB033_Model_1)F2_Model_1).getColumnCount();
                ((KNJB033_Model_1)F2_Model_1).getMaxRow(MaxCol);
                /** テーブル再表示 */
//                ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        		// jTable1 行ヘッダ再描画
//        		Table1Header();
//                jTable2.repaint();
				ReDsp();
                break;
            }
        }

        /** カラムサイズ設定 */
        setColSize();
    }


    /**
     * jTable1ポップアップメニューアクション
     */
    void popup2_actionPerformed(ActionEvent e)
    {
        int MaxCol = ((KNJB033_Model_2)F2_Model_2).getColumnCount();
        String GradeClass; // jTable1で選択されたセルの 学年 + クラス
        String GradeClass2; // jTable2への移動場所指定用 学年 + クラス

        /** 選択行・列取得 */
//        int SelCol2 = jTable1.getSelectedColumn();
//        int SelRow2 = jTable1.getSelectedRow();
        int SelCol2[] = jTable1.getSelectedColumns();
        int SelRow2[] = jTable1.getSelectedRows();
//        System.out.println(SelRow2 + "行 " + SelCol2 + "列");
for(int i = SelRow2.length; i > 0 ; i--) {
        /** 移動生徒情報取得・削除 */
        StudentData SelStudent2 = ((KNJB033_Model_1)F2_Model_1).getStudentData(SelRow2[i-1],SelCol2[0]);
        GradeClass = SelStudent2.getData(0) + SelStudent2.getData(1);
        /** 移動生徒情報設定 */
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
                /** 最大行数更新 */

                int MaxCol2 = ((KNJB033_Model_2)F2_Model_2).getColumnCount();
                ((KNJB033_Model_2)F2_Model_2).getMaxRow(MaxCol2);

                /** テーブル再表示 */
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

        /** カラムサイズ設定 */
        setColSize();
   }

    /**
     * F3Dlgからの戻り
	 * @param	DB2UDB
     */
    public void query2(DB2UDB db2)
    {
        System.out.println("query2 呼び出し確認");
        String attendclasscd;
        int targetCol;
        String sql2;
        int targetCnt = 0;
        ((KNJB033_Model_1)F2_Model_1).AllClear();
        for (int rpCnt = 0; rpCnt < length; rpCnt++)
        {
            try
            {
                /** 受講クラスコード設定 */
                attendclasscd = data2[rpCnt];
                System.out.println("受講クラスコード確認 " + data2[rpCnt] + "rpcnt is " + rpCnt);
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
                /** SQL文の実行 */
                db2.query(sql2);
                /** 実行結果取得 */
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
                    /** 情報設定 */
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

                    /** 生徒情報格納 */
                    ((KNJB033_Model_1)F2_Model_1).setStudent(targetCnt,StrGrade,StrClass,
                    StrAttendNo,StrName,StrSchregno,StrRow,StrColumn,StrAppDate,StrAppEndDate,StrRegisterCD);
                }
                /** COMMIT */
                db2.commit();

            }
            catch (Exception f)
            {
                System.out.println("生徒情報取得エラー = " + f.getMessage());
                break;
            }
        }

        /** 最大行数取得 */
        int MaxCol = ((KNJB033_Model_1)F2_Model_1).getColumnCount();
        /** 最大行数更新 */
        ((KNJB033_Model_1)F2_Model_1).getMaxRow(MaxCol);

        /** カラムサイズ設定 */
        setColSize();
    }

    void jTable1_mouseClicked(MouseEvent e)
    {

    }

    /**
     * 保存ボタン
     */
    void jButton2_actionPerformed(ActionEvent e)
    {
        int number=0;
        // jTable1の列数取得
        int MaxCol = ((KNJB033_Model_1)F2_Model_1).getColumnCount();
        /** 更新関数 */
        boolean flg = ((KNJB033_Model_1)F2_Model_1).setDB(db2);
        /** 戻り値判定 */
        if (flg == true)
        {
            JOptionPane.showMessageDialog(null, "正常に終了しました", "結果",
                                  JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(null, "保存に失敗しました", "結果",
                                  JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 取消ボタン
     */
    void jButton3_actionPerformed(ActionEvent e)
    {
        // jTable1 表示内容クリア
        ((KNJB033_Model_1)F2_Model_1).AllClear();
        // jTable1 SQL実行
        ((KNJB033_Model_1)F2_Model_1).query(db2);
        // jTable1再描画
        ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        /** jTable1の列数分表示(ヘッダ) */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            HdAdd_dat[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd(cnt);
        }
        //ヘッダの追加(jTable1)
        TableColumnModel cm = jTable1.getColumnModel();
        GroupableTableHeader header = (GroupableTableHeader)this.jTable1.getTableHeader();
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            ColumnGroup g_name = new ColumnGroup(HdAdd_dat[cnt]);
            g_name.add(cm.getColumn(cnt));
            header.addColumnGroup(g_name);
        }

        // jTable2 表示用データクリア
        ((KNJB033_Model_2)F2_Model_2).AllClear();
        // jTable2 SQL用変数設定
        ((KNJB033_Model_2)F2_Model_2).setSelData(SelYear, SelSemester, SelChrCD, SelGrpCD, SelStaff, SelClass);
        // jTable2 SQL実行
        ((KNJB033_Model_2)F2_Model_2).query(db2);
        // jTable2 再描画
        ((KNJB033_Model_2)F2_Model_2).fireTableStructureChanged();

        /** カラムサイズ設定 */
        setColSize();
    }

    /**
     * 名簿のコピーからの戻り(テーブルの再描画)
	 * @param	無し
     */
    public void F3Returned()
    {
	  if (KNJB033F1_Dlg1.getcheckflg())
	  {
        length = KNJB033F1_Dlg1.getColCnt();
        // 受講クラスコード取得
        for(int i=0;i<length;i++)
        {
            data2[i] = KNJB033F1_Dlg1.getData(1,i);
        }
        // 対象列番号取得
        for(int j=0;j<length;j++)
        {
            data5[j] = KNJB033F1_Dlg1.gettargetColumn(j);
        }
        // jTable1 SQL実行
        query2(db2);
        // jTable1 再描画
        ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        // jTable1 行ヘッダ再描画
        Table1Header();
        /** jTable1の列数分表示(ヘッダ) */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            HdAdd_dat[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd(cnt);
        }
        //ヘッダの追加(jTable1)
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
		// F3選択群コード取得
		String F3GrpCD = KNJB033F1_Dlg1.F3GetGrpCD();
        // SQL用変数設定
        ((KNJB033_Model_2)F2_Model_2).setSelData(SelYear,SelSemester,SelChrCD,F3GrpCD,SelStaff,tClass);
        // SQL実行
        ((KNJB033_Model_2)F2_Model_2).query(db2);
        // jTable2 再描画
        ((KNJB033_Model_2)F2_Model_2).fireTableStructureChanged();
        // jTable2 行ヘッダ再描画
        Table2Header();

        /** カラムサイズ設定 */
        setColSize();
	  }
    }

    /**
     * jTable1 行ヘッダ追加
	 * @param	無し
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
        rowHeader.setFixedCellWidth(30);	// 行見出しの「幅」
        rowHeader.setFixedCellHeight(jTable1.getRowHeight());
        rowHeader.setBackground(Color.lightGray);
        rowHeader.setCellRenderer(new RowHeaderRenderer(jTable1, SwingConstants.LEFT));
        // 行見出しをスクロール・ペインに設定
        jScrollPane1.setRowHeaderView(rowHeader);
    }

    /**
     * jTable2 行ヘッダ追加
	 * @param	無し
     */
     public void Table2Header()
     {
        // jTable2行ヘッダー設定
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
        rowHeader2.setFixedCellWidth(30);	// 行見出しの「幅」
        rowHeader2.setFixedCellHeight(jTable2.getRowHeight());
        rowHeader2.setBackground(Color.lightGray);
        rowHeader2.setCellRenderer(new RowHeaderRenderer(jTable2, SwingConstants.LEFT));
        // 行見出しをスクロール・ペインに設定
        jScrollPane2.setRowHeaderView(rowHeader2);
    }

    /**
     * カラムサイズ設定
	 * @param	無し
     */
    public void setColSize()
    {
        /** Table1 サイズ設定 */
        TableColumn tc0;
        /** 列数分表示 */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            /** テーブル１カラムサイズセット */
            tc0 = jTable1.getColumnModel().getColumn(cnt);
            tc0.setMinWidth(150);
            tc0.setMaxWidth(Integer.MAX_VALUE);
            tc0.setPreferredWidth(150);
        }
        /** Table2 サイズ設定 */
        int tbl2_ColCnt = ((KNJB033_Model_2)F2_Model_2).getColumnCount();
        TableColumn tc1;
        for (int roopCnt = 0; roopCnt < tbl2_ColCnt; roopCnt++)
        {
            /** テーブル２カラムサイズセット */
            tc1 = jTable2.getColumnModel().getColumn(roopCnt);
            tc1.setMinWidth(150);
            tc1.setMaxWidth(Integer.MAX_VALUE);
            tc1.setPreferredWidth(150);
        }
    }


    /**
     * 再描画
	 * @param	無し
     */
    public void ReDsp()
    {
        // jTable1 再描画
        ((KNJB033_Model_1)F2_Model_1).fireTableStructureChanged();
        // jTable1 行ヘッダ再描画
        Table1Header();
        /** jTable1の列数分表示(ヘッダ) */
        for (int cnt = 0; cnt < ColCnt; cnt++)
        {
            HdAdd_dat[cnt] = ((KNJB033_Model_1)F2_Model_1).getHeadAdd(cnt);
        }
        //ヘッダの追加(jTable1)
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
        // jTable2 再描画
        ((KNJB033_Model_2)F2_Model_2).fireTableStructureChanged();
        // jTable2 行ヘッダ再描画
        Table2Header();
    }
}
