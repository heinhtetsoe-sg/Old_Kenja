
*** OTR(カードリーダー)からの出席データ入力プログラム ***



■OtrRead.tarに含まれるファイルとその内容は以下の通りです。
・OtrRead.x.x.x.jar       ・・・実行jarプログラムファイル (x.x.x:バージョン)
・log4j.properties        ・・・ログ出力フォーマットファイル
・otr_read.sh.DB2v7       ・・・実行スクリプト(DB2v7用)
・otr_read.sh.DB2v8       ・・・実行スクリプト(DB2v8用)
・OtrRead.properties      ・・・プログラムのプロパティーファイル
・PeriodTimeTable.properties ・・・校時のタイムテーブル
・otr_TAG_HISTORY.txt     ・・・更新履歴
・otr_html_spec/OtrRead.html・・・HTML仕様書
・readme.txt

プログラム実行のイメージとしては
インストールで作成するotr_read.sh にDB名の引数を追加して実行しますと、
起動した日付、起動した時間までの校時の、出欠未実施の授業の
出欠データを、OTRが出力したファイルから読み取り、
指定のDBの仮出欠データテーブル(ATTEND2_DAT)に書き込みます。

校時と時間の設定を PeriodTimeTable.properties で行います。
OTRが出力したファイルの場所は OtrRead.properties で指定します。
プログラムのログの設定はlog4j.propertiesで指定します
(ログの場所はデフォルトで/var/tmp/otr_read.xx.logとしています)。


■インストールの手引きを以下に示します。

(1) OtrRead.properties を編集してください。

  kintaiFilePath に OTRが出力する勤怠のファイルのパスを指定します。
    (例: kintaiFilePath = /usr/local/development/DAKINTAI.txt )


(2) PeriodTimeTable.properties を編集します。

  校時の開始時間、終了時間を指定します。次のフォーマットで
  1時数1行の記述でお願いします。
    フォーマット : [時数(名称マスタNAMECD1='B001'のNAMECD2の文字列)] = [開始時間],[終了時間]
    (例: 1 = 08:45,09:35)


(3) otr_read.sh を作成します。

  使用するDB2のバージョンによって
  以下のファイルコピーコマンドを選択し、実行します。
  (a)DB2のバージョンが7
    cp otr_read.sh.DB2v7 otr_read.sh
  (b)DB2のバージョンが8
    cp otr_read.sh.DB2v8 otr_read.sh


(4) otr_read.shに実行モードを追加します

  次のコマンドを実行します。
    chmod +x otr_read.sh


(5) リンクOtrRead.jarを作成します。

  次のコマンドを実行します。(x.x.x:バージョン)
    ln -s OtrRead.x.x.x.jar OtrRead.jar 


(6) cron で指定時間に otr_read.sh が実行されるようにセットします。

	(例: 以下をroot権限で/etc/crontabに追加します。
	(    ※DBNAMEは実際にはDBの名称です
	35  9 * * 0-5 root /tmp/otr_read.sh DBNAME # 月曜~金曜の09:35にrootで/tmp/otr_read.sh DBNAME を実行
	25 10 * * 0-5 root /tmp/otr_read.sh DBNAME #            10:25
	35 11 * * 0-5 root /tmp/otr_read.sh DBNAME #            11:35
	...


(7) cronで指定時間に OTRが出力した勤怠のファイルを削除するようにセットします
    (例: 以下をroot権限で/etc/crontabに追加します。
	(    ※DAKINTAI.txtは実際にはOTRが出力した勤怠のファイルの名称です
	0 0 * * * root rm DAKINTAI.txt # 毎日深夜0時にDAKINTAI.txtを削除する
