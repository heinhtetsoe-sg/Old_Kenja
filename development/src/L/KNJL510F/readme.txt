// kanji=漢字
// $Id: readme.txt 71750 2020-01-15 05:43:05Z nakamoto $

2019/12/13  1.KNJL010Fを元に新規作成

2019/12/13  1.DBエラー修正
            2.受験科目、削除ボタンのエラーチェックの不具合修正

2019/12/16  1.新規ボタンを押したときに取得する管理番号MAXをENTEXAM_KANRI_NO_DATに保持し、付番の際に使用するように修正
            -- entexam_kanri_no_dat.sql (rev.71256)

2019/12/17  1.プレテスト得点の保存先変更
            -- ENTEXAM_APPLICANTBASE_DETAIL_DAT.SEQ’019’⇒’038’に変更
            -- ENTEXAM_APPLICANTBASE_DETAIL_DAT.SEQ’020’⇒’039’に変更

2019/12/24  1.奨学区分の名称マスタ「L025」参照方法変更

2020/01/12  1.テーブル変更に伴う修正
            --修正

2020/01/15  1.受験科目コンボは、受験番号範囲設定された受験型のみ表示する
