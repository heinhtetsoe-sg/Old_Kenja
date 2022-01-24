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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JOptionPane;

/**
 * �u�I���v�R�}���h.
 * �m�F�_�C�A���O��\�����A�u�����v�{�^���������ꂽ��A�I������B
 * �u�����v�ȊO�̃{�^����������Ă��A�������Ȃ��B
 * Singleton�p�^�[���ɂ���āA�C���X�^���X�����𐧌����Ă���B
 * @author	TAMURA Osamu
 * @version	$Id: Exit.java,v 1.2 2002/10/28 05:21:26 tamura Exp $
 */
public class Exit implements ActionListener {
	/** �B��̃C���X�^���X */
	private static final	Exit			INSTANCE = new Exit();
	/** �_�C�A���O�̃^�C�g��*/
	private static final	String			TITLE = "�m�F";
	/** �_�C�A���O�̕\�����e */
	private static final	String[]		MESSAGE = {"�I�����܂��B", "��낵���ł����H", };
	/** JApplet */
	private static		JApplet			japplet;
	/** �_�C�A���O�̐e */
	private static		Container		pane;

	/**
	 * �f�t�H���g�E�R���X�g���N�^.
	 * Singleton�p�^�[���ɂ���āA�C���X�^���X�����𐧌����Ă���B
	 */
	private Exit() {
	}

	/**
	 * �B��̃C���X�^���X��Ԃ�.
	 * @param	pane	�_�C�A���O�̐e
	 * @param	japplet	JApplet
	 * @return			�C���X�^���X
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
