# kanji=漢字
# $Id: readme.txt 76621 2020-09-09 07:21:12Z arakaki $

2017/11/08  1.新規作成（ミライコンパス）
            2.氏名（カナ）の「全角カタカナ」を「全角ひらがな」に変換し取込する。

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/01/23  1.ヘッダ出力を追加

2020/09/09  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/03  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
