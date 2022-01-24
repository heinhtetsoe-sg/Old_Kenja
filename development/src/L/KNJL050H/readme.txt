// kanji=漢字
// $Id: readme.txt 77111 2020-09-25 04:01:14Z arakaki $

KNJL050H
得点入力
-----
KNJL050O を元に作成
-----
アルプ共通部品：csvfilealp.php を使用
-----
学校：法政
-----

2007/11/06
・MEISYOU_GETを使用しない
// 受験科目コンボのリスト表示を変更した。
-- 画面から指定された入試年度、入試制度を条件として、
-- 試験科目データ（ENTEXAM_TESTSUBCLASSCD_DAT）から受験科目を抽出し表示する。
// key項目追加対応
-- 以下のテーブルに入試制度（applicantdiv）が追加された。
-- ・受験コースマスタ（entexam_course_mst）
-- ・満点マスタ（entexam_perfect_mst）
-- ・志望区分マスタ（entexam_wishdiv_mst）
// 出願コースを削除した。
2007/12/02 o-naka 受験番号４桁対応。
2007/12/12 nakamoto 入試制度・入試区分コンボを修正。
2008/01/11 nakamoto 受付番号指定を会場指定に変更。
2008/01/22 nakamoto １行目にカーソルがいかない不具合を修正。

2008/10/31  不要なコンストラクタのカット

2020/09/25  1.csvfilealp.php→csvfile.phpに変更