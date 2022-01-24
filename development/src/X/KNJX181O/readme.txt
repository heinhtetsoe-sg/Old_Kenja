// kanji=漢字
// $Id: readme.txt 76646 2020-09-09 07:37:35Z arakaki $

2011/04/06  1.KNJX181(1.1)を元に新規作成。
            -- XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/09  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/12  1.CSVメッセージ統一(SG社)