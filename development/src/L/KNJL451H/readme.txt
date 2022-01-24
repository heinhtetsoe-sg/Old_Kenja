// kanji=漢字

2021/03/15  1.新規作成

2021/04/23  1.合格・不合格の更新テーブル追加
            -- ENTEXAM_RECEPT_DETAIL_DAT.SEQ=016～018のREMARK3
            2.以下の更新テーブルは合格を不合格で上書きしないように修正
            -- ENTEXAM_APPLICANTBASE_DAT.JUDGEMENT
            3.ENTEXAM_COURSE_MST.TESTDIVは参照しない。
            4.内諾者は無条件に合格とする処理追加
            -- スクリプト「rep-entexam_judge_tmp.sql(rev.f733dee)」フィールド追加（NAIDAKU_FLG）
