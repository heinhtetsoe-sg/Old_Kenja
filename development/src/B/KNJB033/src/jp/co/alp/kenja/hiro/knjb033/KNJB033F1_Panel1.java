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
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

/**
 * �^�C�g��:  ���Ԋ��E���C�Ǘ��V�X�e��
 * ����:
 * ���쌠:   Copyright (c) 2002
 * ��Ж�:
 * @author m.miyagi
 * @version 1.0
 */

public class KNJB033F1_Panel1 extends JPanel {



    TitledBorder title = new TitledBorder("");
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton btn_up = new JButton();
    JButton jButton2 = new JButton();
    String data1[];  // �R�s�[���E�����X�g�\���p
    String data2[];  // ��u�N���X�R�[�h�ێ��p
    String data3[];  // �E���R�[�h�ێ��p
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
        btn_up.setText("��ֈړ�");
        btn_up.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                btn_up_actionPerformed(e);
            }
        });
        jButton2.setText("���ֈړ�");
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
    // ��փ{�^��
    void btn_up_actionPerformed(ActionEvent e)
    {
        //���X�g����I�����ꂽ���ڔԍ����擾
        int select = 0;
        select = jList1.getSelectedIndex();
        //�����I������Ȃ��Ń{�^�����������ꂽ�ꍇ�G���[�\��
        if(select < 0)
        System.out.println("NoSelect");
        //�擪�̍��ڂ��I�����ꂽ�ꍇ�G���[�\��
        else if(select == 0)
            System.out.println("select is top");
        else if(data1[select] == "" || data1[select] == null)
            System.out.println("Nodata");
        else
        {
            //�z��̓���ւ�
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
    // ���փ{�^��
    void jButton2_actionPerformed(ActionEvent e)
    {
        //���X�g����I�����ꂽ���ڔԍ����擾
        int select2 = 0;
        int length = 0;
        length = data1.length;
        select2 = jList1.getSelectedIndex();
        //�����I������Ȃ��Ń{�^�����������ꂽ�ꍇ�G���[�\��
        if(select2 < 0)
            System.out.println("NoSelect");
        //�Ō�̍��ڂ��I�����ꂽ�ꍇ�G���[�\��
        else if(data1[select2+1] == "" || data1[select2+1] == null)
            System.out.println("select is last");
        else
        {
            //�z��̓���ւ�
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
