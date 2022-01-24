# kanji=漢字
# $Id$

2020/12/08  1.新規作成
            -- aft_stat_course_stat_dat.sql (rev.c4dbe16a0)
            -- knje444a_kakidashi_list.sql (rev.3b970c3e7)

2021/02/26  1.テーブル名を変更
              AFT_GRAD_COURSE_DAT -> AFT_GRAD_COURSE_SS_DAT
              AFT_STAT_COURSE_STAT_DAT -> AFT_STAT_COURSE_STAT_SS_DAT
              JOBTYPE_S_MST -> JOBTYPE_SS_MST
            2.テーブル名の変更に伴いカラムが追加されたため、
              CSV出力時に追加したカラムを取り扱うように変更

2021/03/03  1.項目名変更。
            -- 職種コード（大中小） → 職種コード（大）
            -- 職種（大中小）名 → 職種（大）名
            -- 小・細分類コード → 職種コード（細）
            -- 科目超過 → 科目数超過
            -- 小・細分類名称 → 職種（小細）名
