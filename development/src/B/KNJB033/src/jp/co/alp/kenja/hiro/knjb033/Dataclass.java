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

/**
 * タイトル:  時間割・履修管理システム
 * 説明:
 * 著作権:   Copyright (c) 2002
 * 会社名:
 * @author 
 * @version 1.0
 */

public class Dataclass implements Cloneable{

  String groupcd;         // 郡コード


  // コンストラクタ
  /*public Dataclass() {
    kamoku_name = "科目名";
    kaime = "1";
    kikan_start = "期間開始";
    kikan_end = "期間終了";
    TaiClass = "対象クラス";
  }*/

  //////
  public Object clone(){
    try{
      Dataclass wrk = (Dataclass)super.clone();

      return wrk;
    } catch (CloneNotSupportedException e) {
      //ここでエラー処理
      throw new InternalError();
    }
  }
  // 郡コード取得
  public String getgroupcd(){
    return groupcd;
  }
  public void setgroupcd(String value){
    groupcd = value;
  }

}