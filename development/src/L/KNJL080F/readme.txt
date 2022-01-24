// kanji=漢字
// $Id: readme.txt 72184 2020-02-04 09:50:38Z maeshiro $

2016/10/21  1.新規作成
            2.合格コースの表示をカット。
            3.特待生フィールドの参照先を修正。
            -- 修正前：ENTEXAM_APPLICANTBASE_DETAIL_DAT.SEQ'009'のREMARK8
            -- 修正後：ENTEXAM_APPLICANTBASE_DATのJUDGE_KIND
            4.右側の表示を修正。
            -- 修正前・・受験番号：氏名：
            -- 修正後・・受験番号：氏名：（入金予定額）
            -- ※ちなみに左側は、以下の通り。
            -- 左側・・・受験番号：氏名：入金日付（入金額）

2016/10/25  1.入学金の入金処理に再作成。
            2.諸費請求額を修正
            3.右側の更新処理を修正。
            -- 支度金関連フィールドをNULLで更新する。
            -- ENTRANCE_FLG
            -- ENTRANCE_PAY_DIV
            -- ENTRANCE_PAY_DATE
            -- ENTRANCE_PAY_MONEY
            4.前回の修正漏れ
            -- ENTRANCE_DUE_DATE
            -- ENTRANCE_DUE_MONEY
            5.支度金利用者は、●を表示する。

2016/10/27  1.参照テーブル変更に伴い修正
            -- entexam_payment_exemption_mst.sql (1.1)
            -- entexam_payment_item_mst.sql (1.1)
            -- v_entexam_recept_exemption_dat.sql (1.1)

2016/11/06  1.テーブル変更に伴い修正
            2.着金日を追加
            -- ENTEXAM_MONEY_DAT.ENT_PAY_CHAK_DATE

2016/11/18  1.入試回数の参照テーブルを修正
            -- 修正前：ENTEXAM_APPLICANTBASE_DAT.TESTDIV0
            -- 修正後：ENTEXAM_RECEPT_DETAIL_DAT.REMARK1
            2.中学第５回と高校一般の時、GENERAL_FLG='1'の人を除く。

2016/12/19  1.手続区分の更新処理をカット
            -- KNJL082Fで行う。

2017/02/17  1.入学支度金貸付利用者は、入学金に費目マスタの入学支度金貸付金をセット

2017/03/06  1.入試回数に第３回・第４回を追加
            -- 第３回・第４回の試験日は名称マスタ「L044」を参照

2018/03/08  1.入試回数に2次募集第２回を追加
            -- 2次募集第２回の試験日は名称マスタ「L059」を参照

2018/11/06  1.入試区分を数値型でソートに変更

2018/11/22  1.参照テーブルを変更
            --ENTEXAM_RECEPT_DAT → V_ENTEXAM_RECEPT_DAT

2020/02/03  1.中学入試は受験番号(RECEPTNO)でソートする

2019/02/04  1.姉妹減免によるコードを別フィールドにした
            -- 姉妹減免と特待の両方の受験者の対応

