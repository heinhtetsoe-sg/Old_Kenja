# kanji=漢字
# $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

KNJD210M
序列確定処理
-----
学校：京都府
-----
主なテーブル
・RECORD_AVERAGE_CHAIR_DAT
・RECORD_AVERAGE_DAT
・RECORD_RANK_CHAIR_DAT
・RECORD_RANK_DAT


2010/04/23  1.KNJD210Lを元に新規作成。

2010/06/30  1.全科の処理で、NULLの生徒を除く条件について、以下の通り修正。
            -- （修正前）
            --      RECORD_SCORE_DAT の該当するデータが NULL。
            --      但し、そのRECORD_SCORE_DATの講座が成績入力完了データに存在すること。
            -- （修正後）
            --      RECORD_SCORE_DAT の SCORE と VALUE が NULL。

2011/04/27  1.学科毎の序列を追加。共愛からの要望。
            -- ※関連スクリプト
            -- 　・rep-record_rank_chair_dat_rev1.3.sql
            -- 　・rep-record_rank_chair_v_dat_rev1.2.sql
            -- 　・rep-record_rank_dat_rev1.3.sql
            -- 　・rep-record_rank_v_dat_rev1.2.sql

2011/04/28  1.前回の修正(1.4)をカット。

2012/07/11  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/06/10  1.合算科目の教育課程を固定'99'とした。

2013/07/11  1.名称マスタのマスタ化に伴う修正

2015/09/16  1.処理学年コンボの参照テーブルを SCHREG_REGD_GDAT に変更

2017/01/23  1.処理学年コンボに全学年を追加
