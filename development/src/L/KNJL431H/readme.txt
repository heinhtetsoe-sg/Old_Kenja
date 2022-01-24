// kanji=漢字

2020/11/20  1.KNJL041Fをもとに新規作成

2021/04/20  1.コード自動整形
            2.以下の通り修正
            -- 受験型、受験コースコンボの位置変更
            -- 受験コースコンボは空白ありに変更。コース指定された時は第一志望で絞り込む。
            -- ENTEXAM_COURSE_MST.TESTDIVは参照しない。
            -- ENTEXAM_APPLICANTBASE_DAT.JUDGEMENTは更新しない。
            -- 欠席チェックON/OFFしたものだけを更新する。
            3.第一志望はENTEXAM_RECEPT_DETAIL_DAT.SEQ=016を参照する。
