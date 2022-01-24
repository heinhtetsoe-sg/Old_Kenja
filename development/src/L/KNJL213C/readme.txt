# kanji=漢字
# $Id: readme.txt 76749 2020-09-10 10:04:37Z arakaki $

2016/09/15  1.新規作成

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/10/07  1.プレテスト区分、重複受験番号の追加に伴う修正
            -- rep-entexam_applicantbase_pre_dat.sql(rev.70053)

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/04  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
