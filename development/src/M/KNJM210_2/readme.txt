# kanji=漢字
# $Id: readme.txt 56590 2017-10-22 13:01:54Z maeshiro $

2006/08/03 帳票の出力機能を追加。
2006/08/29 m-yama   評価欄の表示内容を追加した。
                    GRAD_VALUE、GRAD_DATE、GRAD_TIME全てがNULLの場合
                    「受」/「受付中」と表示する。

2010/04/22  1.「availheight」を「availHeight」に修正

2010/12/27  1.スクーリングが、重複していた場合は、MAX日付を出力していたがMIN日付に変更

2011/07/06  1.下記の修正をした。
            -- 講座コード頭92は除外 → 91以上除外
            -- CHAIR_CORRES_DATにデータが無い場合を考慮する。

2011/12/26  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
