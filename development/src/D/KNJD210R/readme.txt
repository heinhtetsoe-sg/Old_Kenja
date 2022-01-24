# kanji=漢字
# $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

KNJD210R
序列確定処理
-----
学校：京都西山
-----
更新テーブル
・RECORD_AVERAGE_CHAIR_DAT(_CONV_DAT)
・RECORD_AVERAGE_DAT(_CONV_DAT)
・RECORD_RANK_CHAIR_DAT(_CONV_DAT)
・RECORD_RANK_DAT(_CONV_DAT)
主なテーブル
・PERFECT_RECORD_DAT
主な仕様(開発時点)
【SUBCLASSCD='999999'のデータ仕様】
・欠席科目(考査は*、学期・学年成績はNULL)が１つでもあれば、レコードは作成しない

2015/05/26  1.KNJD210Q(1.5)を元に新規作成

2015/12/18  1.重み付合併設定の元科目を合算科目に含めない

2016/02/25  1.999999のレコードを作成する条件の科目から元科目を除く
            -- NULLの学籍番号を取得するクエリーを修正
            -- 前回の修正漏れ

2017/01/10  1.学年表示をSCHREG_REGD_GDATから取得に変更
