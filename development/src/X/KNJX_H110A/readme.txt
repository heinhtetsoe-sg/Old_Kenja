# kanji=漢字
# $Id: readme.txt 76527 2020-09-07 08:42:55Z arakaki $

2011/05/10  1.KNJX_H110（1.5）を元に新規作成。
            -- プログラムＩＤを変更しただけ。

2012/05/28  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。
            2.科目マスタの存在チェックを追加した。

2012/07/11  1.パラメータuseCurriculumcdを追加

2013/01/12  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/07  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/12  1.CSVメッセージ統一(SG社)

2021/04/27  1.CSVメッセージ統一STEP2(SG社)