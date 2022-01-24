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
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

/**
 * タイトル:  時間割・履修管理システム
 * 説明:
 * 著作権:   Copyright (c) 2002
 * 会社名:
 * @author m.miyagi
 * @version 1.0
 */

public class KNJB033F1_Panel1 extends JPanel {



    TitledBorder title = new TitledBorder("");
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton btn_up = new JButton();
    JButton jButton2 = new JButton();
    String data1[];  // コピー元職員リスト表示用
    String data2[];  // 受講クラスコード保持用
    String data3[];  // 職員コード保持用
    int dataflag=0;
    JScrollPane jScrollPane1 = new JScrollPane();
    JList jList1 = new JList();
    public KNJB033F1_Panel1()
    {
//    super();
        try
        {
        jbInit();
//      query();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public KNJB033F1_Panel1(String title_str)
    {
//    super();
        try
        {
            jbInit();
            setTitle(title_str);
//      query();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        this.setLayout(borderLayout1);
        jPanel1.setPreferredSize(new Dimension(10, 40));
        btn_up.setText("上へ移動");
        btn_up.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                btn_up_actionPerformed(e);
            }
        });
        jButton2.setText("下へ移動");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton2_actionPerformed(e);
            }
        });
        this.add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(btn_up, null);
        jPanel1.add(jButton2, null);
        this.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jList1, null);

        this.setBorder(title);
        setPreferredSize(new Dimension(250,200));
    }

    public void setTitle(String title_str)
    {
        title.setTitle(title_str);
    }
    // 上へボタン
    void btn_up_actionPerformed(ActionEvent e)
    {
        //リストから選択された項目番号を取得
        int select = 0;
        select = jList1.getSelectedIndex();
        //何も選択されないでボタンが押下された場合エラー表示
        if(select < 0)
        System.out.println("NoSelect");
        //先頭の項目が選択された場合エラー表示
        else if(select == 0)
            System.out.println("select is top");
        else if(data1[select] == "" || data1[select] == null)
            System.out.println("Nodata");
        else
        {
            //配列の入れ替え
            String tmp = "";
            String tmp2 = "";
            String tmp3 = "";
            tmp = data1[select-1];
            tmp2 = data2[select-1];
            tmp3 = data3[select-1];
            data1[select-1] = data1[select];
            data2[select-1] = data2[select];
            data3[select-1] = data3[select];
            data1[select] = tmp;
            data2[select] = tmp2;
            data3[select] = tmp3;
            jList1.setListData(data1);
            System.out.println("select Num is " + select);
            System.out.println("select-1 is " + tmp);
        }
    }
    // 下へボタン
    void jButton2_actionPerformed(ActionEvent e)
    {
        //リストから選択された項目番号を取得
        int select2 = 0;
        int length = 0;
        length = data1.length;
        select2 = jList1.getSelectedIndex();
        //何も選択されないでボタンが押下された場合エラー表示
        if(select2 < 0)
            System.out.println("NoSelect");
        //最後の項目が選択された場合エラー表示
        else if(data1[select2+1] == "" || data1[select2+1] == null)
            System.out.println("select is last");
        else
        {
            //配列の入れ替え
            String tmp4 = "";
            String tmp5 = "";
            String tmp6 = "";
            tmp4 = data1[select2+1];
            tmp5 = data2[select2+1];
            tmp6 = data3[select2+1];
            data1[select2+1] = data1[select2];
            data2[select2+1] = data2[select2];
            data3[select2+1] = data3[select2];
            data1[select2] = tmp4;
            data2[select2] = tmp5;
            data3[select2] = tmp6;
            jList1.setListData(data1);
            System.out.println("select Num is " + select2);
            System.out.println("select-1 is " + tmp4);
            System.out.println("data length is " + length);
        }
    }

   public void query()
   {
   }

}
