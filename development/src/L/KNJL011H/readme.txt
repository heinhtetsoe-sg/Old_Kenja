// kanji=漢字
// $Id: readme.txt 56581 2017-10-22 12:37:16Z maeshiro $

KNJL011H
志願者基礎データ登録(個票)
-----
KNJL011O を元に作成
-----
学校：法政
-----

2007/11/01
・出願区分をチェックボックス方式に変更
・生年月日を西暦入力に変更
・塾コンボを追加（PRISCHOOL_MSTを参照）
2007/12/02 o-naka 受験番号４桁対応。
2007/12/07 o-naka 得点データの削除条件を修正。
★ 塾コンボのＳＱＬを修正。
★ 国公私立コンボを追加。
★ 出身学校関連対応。
★ 塾コンボの位置を変更。
2007/12/11 o-naka 高校（内部生）は自動的に合格とするよう修正。
2007/12/12 o-naka 高校（内部生）は自動的に「手続区分－済み」「入学区分－済み」とするよう修正。
2008/01/17 o-naka 高校一般・推薦の場合、デフォルトで第１回の入試区分をチェックONにする。
2008/01/19 o-naka 高校内部生の場合、デフォルトで第１回の入試区分をチェックONにする。

2008/10/27  1.推薦受験有チェックボックスを追加。

2008/10/28  1.氏名欄の入力と同時にかな氏名欄に入力が反映されるようにした。

2008/10/29  1.高校一般入試以外で推薦受験有チェックし更新した場合にエラーメッセージ表示。

2008/10/29  コンストラクタの統一

2008/11/10  1.推薦受験フラグではなく、推薦受験番号を登録するよう変更。

2008/11/11  1.入力値（推薦受験番号）の重複チェックを追加。

2009/01/09  1.不具合修正。推薦受験番号に不正な番号を入力し、エラーメッセージ表示、閉じた時に内申欄の表示が消える。

2009/01/10  1.推薦受験番号を入力あるいは削除した場合、加算点を再計算。

2009/01/13  1.氏名入力支援機能を使用しないようにした。

2009/01/15  1.氏名入力支援機能を使用するようにした。

2014/04/21  1.IEバージョンによる、ポップアップの表示位置修正
