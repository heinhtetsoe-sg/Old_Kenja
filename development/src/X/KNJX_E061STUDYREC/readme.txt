# kanji=漢字
# $Id: readme.txt 76581 2020-09-08 08:35:00Z arakaki $

2011/03/14  1.KNJX_A125Jを元に新規作成

2012/06/27  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。

2013/01/12  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/08  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2020/12/10  1.LASTCOLUMN対策漏れの修正

2021/02/15  1.CSVメッセージ統一(SG社)

2021/04/28  1.CSVメッセージ統一STEP2(SG社)