# kanji=漢字
# $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

KNJD210L
序列確定処理
-----
KNJD210Fを元に作成
-----
学校：鳥取
-----
主なテーブル
・RECORD_AVERAGE_CHAIR_DAT
・RECORD_AVERAGE_DAT
・RECORD_RANK_CHAIR_DAT
・RECORD_RANK_DAT
・RECORD_AVERAGE_CHAIR_V_DAT
・RECORD_AVERAGE_V_DAT
・RECORD_RANK_CHAIR_V_DAT
・RECORD_RANK_V_DAT


2009/08/08  1.KNJD210Fからコピーして新規作成。

2009/08/21  1.以下の通り修正。
            -- 素点・評価ラジオボタンを追加。
            -- RECORD_RANK_V_DATのテーブル追加に伴う修正。
            -- 名称マスタ「D017」NAME1に登録されている科目を除く。
            -- 3/5/全科について、合併元科目を除く条件を削除。

2009/09/07  1.ラジオボタン、チェックボックスにラベルを追加した。

2010/02/09  1.全科の処理で、NULLの生徒を除く条件について、以下の通り修正。
            -- （修正前）
            --      RECORD_SCORE_DATの該当するデータがNULL。
            -- （修正後）
            --      RECORD_SCORE_DATの該当するデータがNULL。
            --      但し、そのRECORD_SCORE_DATの講座がテスト時間割に存在すること。
            ※ 素点を実行した場合のみ。

2010/02/10  1.(2/9)修正の参照テーブル変更。
            -- テスト時間割を成績入力完了データに変更。

2010/03/08  1.以下の通り、「教科９０未満」という条件を追加
            -- 全科の処理で、NULLの生徒を除く条件について、
            --      RECORD_SCORE_DATの該当するデータがNULL。
            --      但し、そのRECORD_SCORE_DATの教科は９０未満とする。

2010/03/18  1.以下の通り、「但し、・・・」という条件を追加
            -- 全科の処理で、NULLの生徒を除く条件について、
            --      RECORD_SCORE_DATの該当するデータがNULL。
            --      但し、そのRECORD_SCORE_DATのCOMP_CREDITとGET_CREDITもNULL。
            ※ 学年末を実行した場合のみ。
            ※ 未履修科目のある生徒を序列対象とするための対応

2010/06/21  1.全科の処理で、NULLの生徒を除く条件について、以下の通り修正。
            -- （修正前）
            --      RECORD_SCORE_DAT の該当するデータが NULL。
            --      但し、そのRECORD_SCORE_DATの講座が成績入力完了データに存在すること。
            -- （修正後）
            --      RECORD_SCORE_DAT の SCORE と VALUE が NULL。

2010/12/14  1.全科の処理で、NULLの生徒を除く条件について、以下の条件を追加。
            -- 「2:評価」実行時、合併元科目を除く。

2012/07/11  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/06/10  1.合算科目の教育課程を固定'99'とした。

2013/07/11  1.名称マスタのマスタ化に伴う修正

