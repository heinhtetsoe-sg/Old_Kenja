// kanji=漢字
// $Id: readme.txt 76267 2020-08-27 08:56:24Z arakaki $

2020/07/16  1.新規作成

2020/07/17  1.DBエラー修正

2020/07/21  1.入力枠を中央揃えとする。
            2.科目・講座のコンボSQL修正（今年度のデータのみ表示）

2020/08/27  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/01  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/01/15  1.CSVメッセージ統一(SG社)