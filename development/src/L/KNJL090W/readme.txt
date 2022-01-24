// kanji=漢字
// $Id: readme.txt 59473 2018-04-03 06:26:18Z nakamoto $

2017/01/11  1.KNJL090Tを元に新規作成

2017/01/14  1.受験番号ゼロ埋めするように修正
            2.かな検索の不具合修正

2017/07/25  1.テーブル変更（APPLICANTDIV追加）に伴い修正

2017/07/26  1.調査書の項目（３Ｔ、５Ｔ、平均）表示カット。文言変更（９Ｔ→合計）

2017/07/27  1.願変の志望コースを優先に参照する。
            -- 願変の志望コースがあれば願変の志望コース、なければ通常の志望コースを参照する。
            2.文言修正（受検番号→受付番号）

2017/07/31  1.欠席日数、欠席理由は下記フィールドとする。
            -- ENTEXAM_APPLICANTCONFRPT_DATの
            -- ABSENCE_DAYS、ABSENCE_DAYS2、ABSENCE_DAYS3
            -- ABSENCE_REMARK、ABSENCE_REMARK2、ABSENCE_REMARK3

2017/11/14  1.手続区分をカット
            -- 入学区分は、合格者の時、選択可能とした。

2018/01/09  1.権限チェック追加

2018/02/28  1.文言修正（受付番号 ⇒ 受検番号）
            2.不合格の時(JUDGEMENT=2)は、辞退(ENTDIV=2)を選択できるようにする
            --合格の時(JUDGEMENT=1,3)に、辞退(ENTDIV=2)を選択し更新をすると、不合格(JUDGEMENT=2)で強制的に更新する
            3.合格更新時に（JUDGEMENT=1,3）、入学コースもセットする（ENTEXAM_APPLICANTBASE_DETAIL_DATのSEQ='007'のREMARK1、REMARK2、REMARK3にセット）
            --不合格更新時に（JUDGEMENT=2）、入学コースをNULLにする
            4.PROCEDUREDIVにENTDIVの値をセットする

2018/03/07  1.権限チェック追加（入試管理者）
            --STAFF_DETAIL_MST(STAFF_SEQ=009)のFIELD1が1以外の時、画面を閉じる

2018/03/16  1.入学コースは、受験コースマスタの入学コースを参照しセットする。

2018/04/02  1.別紙様式３の備考を追加
            --ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ=021)のREMARK10

2018/04/03  1.(別紙様式３)備考を変更
            --ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ=032)のREMARK1
            2.(別紙様式５)転居の年月日、転居地を追加
            --ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ=032)のREMARK2
            --ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ=032)のREMARK3
