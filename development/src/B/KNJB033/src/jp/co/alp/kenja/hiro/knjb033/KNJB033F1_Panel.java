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

import java.awt.*;
import javax.swing.border.*;
import javax.swing.*;

/**
 * タイトル:  時間割・履修管理システム
 * 説明:
 * 著作権:   Copyright (c) 2002
 * 会社名:
 * @author m.miyagi
 * @version 1.0
 */

public class KNJB033F1_Panel extends JPanel {

    TitledBorder title = new TitledBorder("");
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton jButton1 = new JButton();
    JButton jButton2 = new JButton();
    JList jList1 = new JList();

    public KNJB033F1_Panel() {
//        super();
        try
        {
            jbInit();
        }
        catch(Exception e) {
      e.printStackTrace();
    }
  }
  public KNJB033F1_Panel(String title_str) {
//    super();
    try {
      jbInit();
      setTitle(title_str);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    jPanel1.setPreferredSize(new Dimension(10, 40));
    jButton1.setText("上へ移動");
    jButton2.setText("下へ移動");
    this.add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(jButton1, null);
    jPanel1.add(jButton2, null);
    this.add(jList1, BorderLayout.CENTER);

    this.setBorder(title);
    setPreferredSize(new Dimension(250,200));
  }

  //
  public void setTitle(String title_str){
    title.setTitle(title_str);
  }

}
