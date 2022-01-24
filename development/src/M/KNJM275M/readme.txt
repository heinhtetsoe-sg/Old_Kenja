# kanji=漢字
# $Id: readme.txt 74340 2020-05-15 08:38:09Z ooshiro $

2013/05/17  1.新規作成

2013/05/20  1.卒業予定は処理年度のデータのみ出力し、値を'1'として出力
            2.管理者の場合は、処理年度で開講している科目（SUCLASS_YDAT）を表示するよう修正
            
2013/07/02  1.初回受付日をRECEIPTE_INPUT_DATEからRECEIPT_DATEに修正

2014/04/30  1.科目一覧の参照テーブル修正
                - SUBCLASS_MST ⇒ V_SUBCLASS_MST

2020/05/12  1.プロパティ「useRepStandarddateCourseDat」を追加
             -- useRepStandarddateCourseDat = '1'の場合はREP_STANDARDDATE_COURSE_DATテーブルを使用

2020/05/15  1.基準日取得時、課程学科での絞込みを追加
