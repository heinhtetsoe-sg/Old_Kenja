// kanji=漢字
// $Id: readme.txt 56583 2017-10-22 12:43:10Z maeshiro $

2013/11/13  1.KNJL090Rを元に新規作成

2013/11/14  1.修正
            2.単願の処理追加

2013/11/18  1.状態の仕様変更

2013/11/19  1.受付番号追加

2013/11/21  1.合否区分が'1'以外の場合単願手続き日をクリアする。

2013/12/18  1.単願手続区分を1.済みにした時、合格コースを単願合格コースをセットする

2013/12/19  1.合否区分が不合格の場合も単願手続区分と手続日を変更できるよう修正
            2.単願手続区分が1:済みの場合は、合否区分を1:合格に変更する
                - 単願手続区分が1:済みで更新した場合は、合格コースに関わらず全て単願切換合格になる
            3.ENTEXAM_APPLICANTBASE_DATのSPECIAL_MEASURESをカット
            4.単願切換フラグをENTEXAM_RECEPT_DATのPROCEDUREDIV1にて判断する

2013/12/26  1.文言修正

2014/01/27  1.正規合格コース及び単願合格コースに略称も出力する。
            -- ENTEXAM_COURSE_JUDGMENT_MST.JUDGMENT_COURSE_ABBV
            2.前回の不具合修正
            3.以下の通り修正
            -- 文言変更。入学コース→合格コース
            -- 入学コースコンボ追加
            
2014/02/14  1.文言修正
              - 入学区分 ⇒ 入辞区分
            2.特別措置者の表示追加

2014/02/14  1.特別措置者の表示位置変更。文字をオレンジ色に変更。

2014/02/18  1.合格コース、合否区分を表示のみに変更
            2.進学コース（単願切換）の合格者は、判定欄を「不合格→条件付合格」に表示変更

2014/03/12  1.正規合格コースと単願合格コースをカットして、合格コース「高い方の合格コースと合否区分略称」を表示する。

2014/03/19  1.高い方の合格コースの名称をコース判定マスタに変更

2014/03/20  1.上の合格コースはコースマスタからの表示にもどした

2014/03/27  1.上の合格コースをコンボに変更。合格コースマスタの名称と略称を表示。

2014/04/08  1.手続キャンセルした場合（手続区分１以外の時）、受付番号を取消す。

2014/04/21  1.IEバージョンによる、ポップアップの表示位置修正

2014/05/07  1.文言修正
            -- 上の合格コース ⇒ 入学区分
