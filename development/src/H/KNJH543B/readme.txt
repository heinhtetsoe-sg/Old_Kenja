# kanji=漢字
# $Id: readme.txt 56588 2017-10-22 12:57:09Z maeshiro $

KNJH543B
実力科目合併先素点生成処理
-----
学校：明治学園
-----
更新テーブル
・PROFICIENCY_COMB_GCALC_EXEC_DAT
・PROFICIENCY_DAT
参照テーブル
・PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT
・PROFICIENCY_SUBCLASS_MST
・PROFICIENCY_YMST
・PROFICIENCY_MST
・COURSE_GROUP_DAT

2012/04/05  1.KNJD219A、KNJH543Aを元に新規作成

2012/04/11  1.合併設定（親・子・孫）の対応
            -- 子のチェックボックスは表示しない
            -- 親の実行時、子を先に生成した後、親を生成する

2012/04/13  1.PROFICIENCY_DAT.REMARK1追加に伴い修正
            -- CASE WHEN MAX(SCORE_DI) = '*' THEN MAX(SCORE_DI) ELSE MAX(REMARK1) END AS REMARK1

2012/04/14  1.計算方法(1:平均値)追加
            2.元科目に１つでも欠試(*)があれば、先科目の得点はNULLとする
            3.REMARK1の更新をカットし、SCORE_DIの更新に変更

2012/04/16  1.PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT.WEIGHTING追加に伴い修正
            -- 重みがあれば、元科目の得点は「得点×重み」とする
            
2012/05/30  1.テーブル名変更
              - COURSE_GROUP_DAT → COURSE_GROUP_CD_DAT
              - COURSE_GROUP_HDAT → COURSE_GROUP_CD_HDAT
              
2012/07/03  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
                
2013/09/04  1.教育課程対応不必要の為、修正カット

2015/03/05  1.プロパティ「weightingHyouki」= '1'のとき、小数換算するため割る100を行う