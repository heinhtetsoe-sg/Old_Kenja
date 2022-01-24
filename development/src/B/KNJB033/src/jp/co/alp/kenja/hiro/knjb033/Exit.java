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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JOptionPane;

/**
 * 「終了」コマンド.
 * 確認ダイアログを表示し、「了解」ボタンが押されたら、終了する。
 * 「了解」以外のボタンを押されても、何もしない。
 * Singletonパターンによって、インスタンス生成を制限している。
 * @author	TAMURA Osamu
 * @version	$Id: Exit.java,v 1.2 2002/10/28 05:21:26 tamura Exp $
 */
public class Exit implements ActionListener {
	/** 唯一のインスタンス */
	private static final	Exit			INSTANCE = new Exit();
	/** ダイアログのタイトル*/
	private static final	String			TITLE = "確認";
	/** ダイアログの表示内容 */
	private static final	String[]		MESSAGE = {"終了します。", "よろしいですか？", };
	/** JApplet */
	private static		JApplet			japplet;
	/** ダイアログの親 */
	private static		Container		pane;

	/**
	 * デフォルト・コンストラクタ.
	 * Singletonパターンによって、インスタンス生成を制限している。
	 */
	private Exit() {
	}

	/**
	 * 唯一のインスタンスを返す.
	 * @param	pane	ダイアログの親
	 * @param	japplet	JApplet
	 * @return			インスタンス
	 */
	public static Exit getInstance(JApplet japplet, Container pane) {
		Exit.japplet = japplet;
		Exit.pane = pane;
		return INSTANCE;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		int ans = JOptionPane.showConfirmDialog(pane, MESSAGE, TITLE, JOptionPane.OK_CANCEL_OPTION);
		if (JOptionPane.OK_OPTION == ans) {
			if (null != japplet) {
				String s = japplet.getCodeBase() + "done.html";	// Ex)"http://hiro/development/B/KNJB/KNJB033/done.html";
				try {
					java.net.URL url = new java.net.URL(s);
					japplet.getAppletContext().showDocument(url);
				} catch (java.net.MalformedURLException ex) {
					System.out.println(s);
					ex.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				System.exit(0);
			}
		}
	}
}
