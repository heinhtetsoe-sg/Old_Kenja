// kanji=漢字
// $Id: readme.txt 77096 2020-09-25 02:32:10Z arakaki $

KNJL010H
志願者基礎データ登録(一覧)
-----
KNJL010O を元に作成
-----
アルプ共通部品：csvfilealp.php を使用
-----
学校：法政
-----

2007/10/30
・ヘッダー部分の出願区分初期値をカット
・一覧部分の出願区分・出願コースをカット
・一覧部分に入試区分を追加
・CSVレイアウト変更
※その他
・MEISYOU_GETを使用しない
2007/12/02 o-naka 受験番号４桁対応。
2007/12/06 o-naka 入試制度コンボ。初期値表示対応。
2007/12/06 o-naka 出身学校・国公私立の対応。
2007/12/07 o-naka 得点データの削除条件を修正。
2007/12/11 o-naka 高校（内部生）は自動的に合格とするよう修正。
2007/12/12 o-naka 高校（内部生）は自動的に「手続区分－済み」「入学区分－済み」とするよう修正。
2007/12/12 o-naka 既に登録済の番号で入試制度を間違って指定し読込んだ場合エラーメッセージを表示する。
2008/01/17 o-naka 高校一般・推薦の場合、デフォルトで第１回の入試区分をチェックONにする。
2008/01/19 o-naka 高校内部生の場合、デフォルトで第１回の入試区分をチェックONにする。

2008/10/24  1.推薦受験有チェックボックスを追加。

2008/10/29  1.高校一般入試以外で推薦受験有チェックし更新した場合にエラーメッセージ表示。

2008/10/30  表示される年度のずれを修正

2008/11/10  1.推薦受験フラグではなく、推薦受験番号を登録するよう変更。

2008/11/11  1.入力値（推薦受験番号）の重複チェックを追加。

2009/01/15  1.氏名入力支援機能を使用するようにした。

2009/02/23  1.ＣＳＶ取込ができない不具合を修正した。

2020/09/25  1.csvfilealp.php→csvfile.phpに変更