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
 * 名簿のコピー画面(LHA020F3)
 * @author m.miyagi
 * @version 1.0
 */

public class KNJB033F1_Dlg extends JDialog {
    DB2UDB      db2;        // Databaseクラスを継承したクラス
    ResultSet   SqlResult;  // ＤＢの検索結果
    Vector data = new Vector();
//    Vector test = new Vector();
    int flag=0; //コンボボックス表示用フラグ
    private boolean chkflg; // 選択フラグ
    String subcode;  // 科目コード1
    String subcode2; // 科目コード2
    // 各データ保持用変数
    String groupcd;      // 郡コード
    String gname;        // 郡名称
    String TaiClass;     // 対象クラス
    String year;  // 年度
	String SelGrpCD; // 群コード

     public Object clone(){
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

    /**
     * 群コード設定処理
	 * @param	int
    */
    public void set_groupcd(String value){
        groupcd = value;
    }

    /**
     * 選択科目名称設定処理
	 * @param	String
    */
    public void set_gname(String value){
        gname = value;
    }

    /**
     * 対象クラス設定処理
	 * @param	String
    */
    public void set_TaiClass(String value){
        TaiClass = value;
    }

    /**
     * 年度設定処理
	 * @param	String
    */
    public void set_year(String value){
        year = value;
    }

	/**
	 * 選択群コード取得関数
	 * @param	無し
	*/
	public String F3GetGrpCD()
	{
		return SelGrpCD;
	}

	/**
	 * カラム数取得関数
	 * @param	無し
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
	 * 対象カラム取得関数
	 * @param	int
	*/
    public int gettargetColumn(int tcol)
    {
        int targetcol;
        targetcol = f3pane2.data5[tcol];
        return targetcol;
    }

	/**
	 * 郡コード取得関数
	 * @param	無し
	*/
    public String getGrpCD()
    {
        return groupcd;
    }

	/**
	 * 対象クラス取得関数
	 * @param	無し
	*/
    public String gettergetClass()
    {
        return TaiClass;
    }

	/**
	 * 対象データ取得関数
	 * @param	無し
	*/
    public String getData(int index,int count)
    {
        String data2;
        data2 = f3pane.data2[count]; // 受講クラスコード
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
    KNJB033F1_Panel1 f3pane = new KNJB033F1_Panel1("(F)コピー元職員リスト");
    KNJB033F1_Panel2 f3pane2 = new KNJB033F1_Panel2("(T)コピー先職員リスト");
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
        jLabel1.setText("左右の職員リストを上下ボタンで対応する");
        jButton1.setMaximumSize(new Dimension(87, 35));
        jButton1.setMinimumSize(new Dimension(87, 35));
        jButton1.setPreferredSize(new Dimension(87, 35));
        jButton1.setText("コピー(C)");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                jButton1_actionPerformed(e);
            }
        });
        jButton2.setMaximumSize(new Dimension(109, 35));
        jButton2.setMinimumSize(new Dimension(109, 35));
        jButton2.setPreferredSize(new Dimension(109, 35));
        jButton2.setText("キャンセル(X)");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton2_actionPerformed(e);
            }
        });
        jPanel5.setLayout(borderLayout4);
        jLabel2.setFont(new java.awt.Font("Dialog", 1, 30));
        jLabel2.setText("→");
        jPanel3.setLayout(gridBagLayout1);
        jLabel3.setText("職員順に並べ替えて下さい。");
        jLabel4.setText("注）溢れた職員は割り振られません。");
        jPanel4.setLayout(gridBagLayout2);

        jComboBox1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jComboBox1_actionPerformed(e);
            }
        });
        this.setModal(true);
        this.setTitle("名簿のコピー");
        getContentPane().add(panel1);
        panel1.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(jComboBox1, BorderLayout.CENTER);
        jPanel1.setBorder(new TitledBorder("同じパターン(クラス構成)の選択科目リスト"));
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

        //リストにデータを表示
        jPanel5.add(f3pane,BorderLayout.WEST);
        jPanel5.add(f3pane2,BorderLayout.EAST);

        panel1.add(jPanel5, BorderLayout.CENTER);
        jPanel5.add(jLabel2, BorderLayout.CENTER);
  }

    /**
     * DB接続設定
	 * @param　DB2UDB
    */
    public void setDB(DB2UDB db)
    {
        db2 = db;
    }

    /**
     * コピーボタン押下処理
	 * @param　ActionEvent e
    */
    void jButton1_actionPerformed(ActionEvent e)
    {
        // フラグ設定
        chkflg = true;
        /** 非表示 */
        this.setVisible(false);
        /** Dialog破棄 */
        this.dispose();
    }

    /**
     * コンボボックス選択処理
	 * @param　ActionEvent e
    */
    void jComboBox1_actionPerformed(ActionEvent e)
    {
        int Cselect = 0,grpseq=0;
        String grpcd="0000";
        // コンボボックスより選択された行番号を取得
        Cselect = jComboBox1.getSelectedIndex();
		if (Cselect <= 0)
		{
			return;
		}

        // Vectorよりデータ取得
        Dataclass rowData = (Dataclass)data.elementAt(Cselect-1);
        grpcd = rowData.getgroupcd();  // 郡コード
System.out.println("選択行 = "+ Cselect);
System.out.println("群コード= " + grpcd);
		// 選択群コード設定
		SelGrpCD = grpcd;

        String lname;                  // 職員苗字
        String fname;                  // 職員名
        String subname;                // 科目名
        String attendcd2;              // 受講クラスコード
        String staffcd2;               // 職員コード
        String target2;                // 対象クラス

        int k=0,lng=0,c=0;

        // コピー元職員リスト表示内容の変更
        /** SQL文の作成 */if(flag==1){
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
System.out.println("F3Dialog 発行SQL = " + sql3);
        try
        {
            /** SQL文の実行 */
            db2.query(sql3);

            /** SQL実行結果取得 */
            SqlResult = db2.getResultSet();

            // 表示用データクリア
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

                // 表示用データ格納
                f3pane.data1[k] = lname + "　" + fname +
                "　　" + SqlResult.getString("SUBCLASSNAME");
                // 受講クラスコード格納
                f3pane.data2[k] = attendcd2;
                // 職員コード格納
                f3pane.data3[k] = staffcd2;
                k++;
                f3pane.jList1.setListData(f3pane.data1);
            }
            /** COMMIT */
            db2.commit();
        }
        catch ( Exception ex)
        {
            /** データ取得エラー */
            System.out.println("最終エラー");
        }}
    }

    /**
     * キャンセルボタン押下処理
	 * @param　ActionEvent e
    */
    void jButton2_actionPerformed(ActionEvent e)
    {
        chkflg = false;
        dispose();
    }

    /**
     * コンボボックス表示処理
	 * @param　DB2UDB
    */
    public void query(DB2UDB db2)
    {
        String StrDate1,StrDate2,StrDate3;  /* 開始日付編集用 */
        String EndDate1,EndDate2,EndDate3;  /* 終了日付編集用 */
        String StrNendo;    /* 年度格納変数 */
        String StrGrpcd;    /* 群コード */
        String StrSdate;    /* 開始日付 */
        String StrEdate;    /* 終了日付 */
        String StrClass;    /* 対象クラス */
        String StrName;     /* 選択科目名称 */
        int j=0;
        int gcd=0,gcd4=0;
        String test;

//		jComboBox1.removeAllItems();
        /** 年度コンボボックス表示データ取得 */
        /** SQL文の作成 */
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
System.out.println("コピーコンボ取得SQL=" + sql);
        try
        {
            /** SQL文の実行 */
            db2.query(sql);
            /** SQL実行結果取得 */
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

                /** 開始日付編集 */
                StrDate1 = StrSdate.substring(0,4);
                StrDate2 = StrSdate.substring(5,7);
                StrDate3 = StrSdate.substring(8,10);
                /* 終了日付編集 */
                EndDate1 = StrEdate.substring(0,4);
                EndDate2 = StrEdate.substring(5,7);
                EndDate3 = StrEdate.substring(8,10);

System.out.println("データ追加");
                //** 対象年度指定コンボボックスに値を設定する */
                /** データ追加 */
                jComboBox1.addItem("選択科目［" + StrName + "］");


            }
            flag=1;
            /** COMMIT */
            db2.commit();
        }
        catch (Exception e)
        {
            /** 年度データ取得エラー */
            System.out.println("年度データ取得エラー");
        }

    }

    /**
     * コピー元職員リスト表示処理
	 * @param　DB2UDB
    */
    public void query2(DB2UDB db2){
        String staffcd;  // 職員コード
        String lname;    // 職員苗字
        String fname;    // 職員名
        String subname;  // 科目名
        String attendcd; // 受講クラスコード
        String target;   // 対象クラス

        int i=0;
      /** SQL文の作成 */
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
            /** SQL文の実行 */
            db2.query(sql2);

            /** SQL実行結果取得 */
            SqlResult = db2.getResultSet();

            while( SqlResult.next() )
            {
                // 実行結果よりデータを取得
                lname = SqlResult.getString("LNAME_SHOW");
                fname = SqlResult.getString("FNAME_SHOW");
                subname = SqlResult.getString("SUBCLASSNAME");
                subcode = SqlResult.getString("SUBCLASSCD");
                attendcd = SqlResult.getString("ATTENDCLASSCD");
                TaiClass = SqlResult.getString("TARGETCLASS");
                staffcd = SqlResult.getString("STAFFCD");

                // 表示用データ格納
                f3pane.data1[i] = lname + "　" + fname +"　" + subname;
                // 受講クラスコード格納
                f3pane.data2[i] = attendcd;
                // 職員コード格納
                f3pane.data3[i] = staffcd;
                i++;
            }

            /** COMMIT */
            db2.commit();
        }
        catch ( Exception e)
        {
            /** データ取得エラー */
            System.out.println("コピー元職員リスト 受講クラス名簿データ取得エラー");
        }
       f3pane.jList1.setListData(f3pane.data1);
    }

    /**
     * コピー先職員リストのデータを格納処理
	 * @param　int
	 * @param　String
    */
    public void set_data4(int i,String value)
    {
        f3pane2.data4[i] = value;
        f3pane2.data5[i] = i;
    }

    /**
     * コピー先職員リスト表示処理
	 * @param　無し
    */
    public void SetRightPanel()
    {
        f3pane2.jList1.setListData(f3pane2.data4);
    }

    /**
     * 選択フラグ取得処理
	 * @param　無し
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
     * データクリア処理
	 * @param　無し
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
