// kanji=漢字
// $Id: readme.txt 77106 2020-09-25 02:45:54Z arakaki $

2013/11/08  1.新規作成

2013/11/12  1.対象データを取得する際、満点マスタは志望コースを参照しないよう修正

2013/11/14  1.エラーの文言変更

2013/11/18  1.以下の修正をした。
            -- SCORE2、SCORE3の更新処理カット
            -- CSV出力ファイル名に科目名と会場名を入れる

2013/11/21  1.最大50件ずつ表示するよう修正
                - 50件以上の場合、ボタンにより50件の受験番号ごとに切り替える

2013/12/17  1.画面レイアウト修正
            2.Enterキーにて、得点入力カーソル移動
            3.得点入力欄に'*'を入力できるよう修正
                - '*'の時、ATTEND_FLG = '0'で更新
                - 未入力は不可
            4.入力後、終了ボタンを押下した場合は、メッセージを表示

2013/12/24  1.70人表示の次画面遷移

2013/12/26  1.会場コンボ切換時に受験番号の開始をセットしないよう修正
            2.表示に関する不具合修正(先にソートした後に70件を取得する)

2014/02/14  1.特別措置者のみチェックボックス追加

2016/01/10  1.全部空白（得点または＊が1件も入力無）の時、削除する。

2020/09/25  1.csvfilealp.php→csvfile.phpに変更