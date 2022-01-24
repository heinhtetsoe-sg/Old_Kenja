// kanji=漢字
// $Id: readme.txt 72186 2020-02-04 09:53:18Z maeshiro $

2016/10/25  1.新規作成
            -- 諸費納入金の入金処理
            2.支度金利用者は、●を表示する。

2016/10/27  1.参照テーブル変更に伴い修正
            -- entexam_payment_exemption_mst.sql (1.1)
            -- entexam_payment_item_mst.sql (1.1)
            -- v_entexam_recept_exemption_dat.sql (1.1)

2016/11/06  1.テーブル変更に伴い修正
            2.着金日を追加
            -- ENTEXAM_MONEY_DAT.EXP_PAY_CHAK_DATE

2016/11/18  1.入試回数の参照テーブルを修正
            -- 修正前：ENTEXAM_APPLICANTBASE_DAT.TESTDIV0
            -- 修正後：ENTEXAM_RECEPT_DETAIL_DAT.REMARK1
            2.中学第５回と高校一般の時、GENERAL_FLG='1'の人を除く。

2017/03/06  1.入試回数に第３回・第４回を追加
            -- 第３回・第４回の試験日は名称マスタ「L044」を参照

2018/03/08  1.入試回数に2次募集第２回を追加
            -- 2次募集第２回の試験日は名称マスタ「L059」を参照

2018/11/06  1.入試区分を数値型でソートに変更

2018/11/22  1.参照テーブルを変更
            --ENTEXAM_RECEPT_DAT → V_ENTEXAM_RECEPT_DAT

2019/02/19  1.中学校の表示順を受験番号(RECEPTNO)に変更

2020/02/04  1.姉妹減免によるコードを別フィールドにした
            -- 姉妹減免と特待の両方の受験者の対応

