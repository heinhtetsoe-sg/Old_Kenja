# kanji=漢字
# $Id: readme.txt 75967 2020-08-12 06:32:03Z yamashiro $

2014/10/06  1.KNJA032(1.14)を元に作成

2014/10/09  1.プロパティuseSpecial_Support_School = 1の時、複式クラスを参照する
            -- SCHREG_REGD_GHR_HDAT、SCHREG_REGD_GHR_DAT
            2.SCHREG_REGD_HDATの内、V_名称マスタ「A032」の年組は対象外
            -- 追加として、SCHREG_REGD_GHR_HDATの複式クラス名も表示する

2014/10/20  1.プロパティ名を変更
            -- useSpecial_Support_School
            -- ↓
            -- useSpecial_Support_Hrclass
            2.起動時チェック追加
            -- プロパティ（useSpecial_Support_School = 1）かつ
            -- プロパティ（useSpecial_Support_Hrclass = 1）
            -- 以外の時、エラーメッセージ表示し、画面を閉じる。
            ※ 名称マスタ「A032」関連

2020/08/12  1.最終学期でのログインのみ有効のメッセージの統一。MSG300 → MSG311
            -- View.php(rev.75964)