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

/**
 * �^�C�g��:  ���Ԋ��E���C�Ǘ��V�X�e��
 * ����:
 * ���쌠:   Copyright (c) 2002
 * ��Ж�:
 * @author 
 * @version 1.0
 */

public class Dataclass implements Cloneable{

  String groupcd;         // �S�R�[�h


  // �R���X�g���N�^
  /*public Dataclass() {
    kamoku_name = "�Ȗږ�";
    kaime = "1";
    kikan_start = "���ԊJ�n";
    kikan_end = "���ԏI��";
    TaiClass = "�ΏۃN���X";
  }*/

  //////
  public Object clone(){
    try{
      Dataclass wrk = (Dataclass)super.clone();

      return wrk;
    } catch (CloneNotSupportedException e) {
      //�����ŃG���[����
      throw new InternalError();
    }
  }
  // �S�R�[�h�擾
  public String getgroupcd(){
    return groupcd;
  }
  public void setgroupcd(String value){
    groupcd = value;
  }

}