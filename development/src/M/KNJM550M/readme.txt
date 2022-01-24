# kanji=漢字
# $Id: readme.txt 76738 2020-09-10 08:30:05Z arakaki $

2014/07/25  1.新規作成(処理：新規　レイアウト：KNJM550)

2014/07/28  1.試験、学年成績の場合の参照フィールド変更
            2.CSV処理修正
            3.CSV項目名修正
            4.名簿印刷用にPRGIDをKNJM550→KNJM550Mに変更
            5.帳票コール用のPRGIDを修正
            6.備考サイズチェック修正

2014/07/30  1.角２の印刷条件項目の変更。

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/04  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
