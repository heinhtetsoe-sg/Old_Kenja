// kanji=漢字
// $Id: readme.txt 77118 2020-09-25 04:04:16Z arakaki $

2015/09/25  1.新規作成

2015/09/28  1.エラー時は画面の値をセットする

2015/09/30  1.受験番号範囲の指定をカット

2015/10/28  1.特別措置者のみチェックボックスをカット
            -- SPECIAL_REASON_DIV

2017/02/11  1.評価欄に「小文字の*」を入力可能に修正
            -- 音読評価の更新不具合修正
            2.面接・音読テキストを(45→90)バイトに変更
            3.評価欄について、「数字の1～5」ではなく「英字のA～D」を入力に変更

2018/01/26  1.ＣＳＶ出力ボタン追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/09/25  1.csvfilealp.php→csvfile.phpに変更

2020/12/03  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
