# kanji=漢字
# $Id: readme.txt 76721 2020-09-10 08:08:53Z arakaki $

2020/01/21  1.KNJL140Bを元に新規作成

2020/02/01  1.CSV対象条件変更。PROCEDUREDIV = '1' → PROCEDUREDATE is not null

2020/02/27  1.入学者のCSV出力で、UseMusyouKaisuプロパティが立っていた場合、無償回数を出力するよう、追加

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/04  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
