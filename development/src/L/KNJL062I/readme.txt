// kanji=漢字
// $Id$

2020/08/18  1.新規作成

2020/09/23  1.以下の修正をした
            --ヘッダ出力時に必須項目に※印を追加
            --必須チェックをする項目を変更
            --高等部志望順位の数値チェック追加
            --受験番号上6桁と名称マスタL071のコードとが一致するかチェックするよう修正
            2.以下の修正をした
            --「名称マスタ」から「入試設定マスタ」への参照先変更に伴う修正
            --入試年度(CTRL_YEAR + 1)を参照するよう修正

2020/10/06  1.リファクタリング
            2.受験番号上6桁のチェック処理をカット
            3.前回の修正漏れを修正

2020/10/06  1.以下の修正をした
            --取込時半角チェックの処理を変更
            --住所を50文字(150バイト)まで取り込めるよう修正

2020/10/14  1.CSV出力時のファイル名を <入試年度>年度入試<メニュー名>（<入試制度名><入試区分名>）.csvに変更

2020/10/19  1.入試制度コンボ、入試区分コンボをVALUE昇順でソートするように修正

2020/12/04  1.卒業年の元号コードが取り込めない不具合修正

2020/12/09  1.中高で共通レイアウトのCSVを取り込むよう修正

2020/12/14  1.帰国性入試の場合には郵便番号の必須チェックをしないよう修正

2020/12/17  1.チェック文字サイズを修正
            2.カラム名を変更
            3.不要なエラーチェック処理を削除
