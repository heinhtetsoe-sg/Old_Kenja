// kanji=漢字
// $Id: readme.txt 77113 2020-09-25 04:02:29Z arakaki $

2013/10/22  1.新規作成

2013/10/24  1.推薦の場合は、L009の予備2＝'1'の科目を表示

2013/11/14  1.エラーの文言変更

2013/11/29  1.「かな」→「カナ」に変更

2013/12/09  1.得点入力欄に'*'を入力できるよう修正
                - '*'の時、ATTEND_FLG = '0'で更新
                - 未入力の時、ATTEND_FLG = NULLで更新

2013/12/11  1.CSV取込で得点欄は、文字列可能とした。

2018/12/07  1.キー["APPLICANTDIV"]追加に伴う修正

2019/01/07  1.入試制度=2の時、入試区分は名称マスタL024を参照するよう、修正
            2.入試制度=2の時、受験科目は名称マスタL009のNAME2がNOT NULLかつ
              NAMESPARE1が入試区分と一致するデータを取得するよう、修正。

2019/09/13  1.縦スクロールが表示されない不具合を修正

2019/09/17  1.入試制度をログイン校種で制御する。
            -- H:APPLICANTDIV = 1
            -- J:APPLICANTDIV = 2

2020/09/25  1.csvfilealp.php→csvfile.phpに変更