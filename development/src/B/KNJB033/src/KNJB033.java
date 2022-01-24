// kanji=漢字
/*
 * $Id: KNJB033.java,v 1.1 2004/06/03 06:54:52 tamura Exp $
 *
 * 作成日: 2004/06/03 15:22:29 - JST
 * 作成者: teruya
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
 * 選択科目実施期間登録(KNJB033)
 * @author      M.Miyagi
 * @version $Id: F1.java,v 1.21 2002/11/05 14:01:55 Miyagi Exp $
 */
public class KNJB033 extends JApplet {
    private KNJB033_Dlg          _form1;
    /** Databaseクラスを継承したクラス */
    DB2UDB      db2;
    /** ＤＢの検索結果 */
    ResultSet   SqlResult;
    /** SQL文作成用 */
    String      sql;
    /** 現在年度 */
    String      NowYear;
    /** 選択年度 */       // 2004-05-08 insert by teruya
    String      SelYear;  // 2004-05-08 insert by teruya
    /** 更新件数 */
    int         UpdateCnt;
    /** 削除件数 */
    int         DeleteCnt;
    /** 「カレンダー」ダイアログ */
    CalendarFrame calendarFrame;
    /** 現在日付 */
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
     * 引数値の取得
     * @param   String      key
     * @param   String      def
     */
    public String getParameter(String key, String def) {
        return isStandalone ? System.getProperty(key, def) :
          (getParameter(key) != null ? getParameter(key) : def);
    }

    /**
     * アプレットの構築
     * @param   無し
     */
    public KNJB033() {
    }


    /**
     * destroy処理
     * @param   無し
     * @see java.applet.Applet#destroy()
     */
    public void destroy() {
        super.destroy();
        System.out.println("destroy");
    }

    /**
     * @see java.applet.Applet#init()
     * @param   無し
     */
    public void init() {
        
        /** データベースに接続 */
        String host   = getParameter("host");             // ホスト名
        String dbname = getParameter("dbname");           // ＤＢ名
        String dbuser = getParameter("dbuser");           // ＤＢユーザ名
        String dbpass = getParameter("dbpass");           // ＤＢパスワード
        String StrYear = getParameter("year");            // 年度
        String StrSemester = getParameter("semester");    // 学期
        String StrChrCD = getParameter("chaircd");        // 講座コード
        String StrGrpCD = getParameter("groupcd");        // 群コード
        String StrGrpName = getParameter("groupname");    // 群名称
        String StrStaff = getParameter("staffcd");          // 職員コード
        if (null == host)    { host = "192.168.50.125"; }
        if (null == dbname)  { dbname = "demodb"; }
        if (null == dbuser)  { dbuser = "db2inst1"; }
        if (null == dbpass)  { dbpass = "db2inst1"; }
        if (null == StrYear) { StrYear = "2002"; }
        if (null == StrSemester) { StrSemester = "2"; }
        if (null == StrChrCD) { StrChrCD = "0000"; }
        if (null == StrGrpCD) { StrGrpCD = "2101"; }
        if (null == StrGrpName) { StrGrpName = "テスト群"; }
        dbname = "//" + host + "/" + dbname;

        db2 = new DB2UDB(dbname, dbuser, dbpass, DB2UDB.TYPE3);
        try
        {
            db2.open();
        }
        catch( Exception ex )
        {
            System.out.println("データベース接続エラー");
            return; // 以降、処理しない
        }


        /** 選択情報設定 */
        _form1 = new KNJB033_Dlg(getContentPane(), this, "", false,StrYear,StrSemester,StrChrCD,StrGrpCD,StrGrpName,StrStaff,db2);
        //KNJB033_Dlg1.query();
        /** 名簿入力画面表示 */
//        KNJB033_Dlg1.show() ;
    }

    /**
     * バージョン情報処理
     * @param 無し
     */
    void version_action() {
        JOptionPane.showMessageDialog(null, "選択科目実施期間登録　Ver1.0", "TITLE",
                              JOptionPane.INFORMATION_MESSAGE);
    }

   /**
     * バージョン情報処理(メニュー)
     * @param ActionEvent e
     */
    void jMenuItem10_actionPerformed(ActionEvent e) {
        version_action();
    }
}

// KNJB033

// eof