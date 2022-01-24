# kanji=漢字
# $Id: c9f7dc29a6ddc99e26ccf27204fa6db2af99566c $

2020/07/08  1.新規作成

2020/09/09  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/10/06  1.CSVデータ取得SQLを修正(CHAIR_CLS_DATを参照しない、組番号の切り出しを修正)

2020/10/13  1.文字コード変換関数でエラーになっていたので修正
            2.講座コードがNULLの場合はスキップするよう、修正

2020/12/07  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/17  1.CSVメッセージ統一(SG社)

2021/04/23  1.CSVメッセージ統一STEP2(SG社)