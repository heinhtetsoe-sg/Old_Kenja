# kanji=漢字
# $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

KNJD219B
学期成績自動算出
-----
学校：明治学園
-----
更新テーブル
・SUBCLASS_SEMVAL_ADJUST_DAT
・SUBCLASS_SEMVAL_PERCENT_DAT
・RECORD_SCORE_DAT
主なテーブル
・PERFECT_RECORD_DAT
・SUPP_EXA_DAT
・PROFICIENCY_DAT
・PROFICIENCY_YMST
・PROFICIENCY_MST
・PROFICIENCY_SUBCLASS_YDAT
・PROFICIENCY_PERFECT_COURSE_DAT

2012/03/05  1.KNJD219Cを元に新規作成

2012/03/08  1.実力テストの対応

2012/03/14  1.制限付の場合、科目コンボはログイン先生の担当科目のみ表示
            2.前回の修正忘れ

2012/04/06  1.実力科目コードを取得するテーブル変更に伴い修正
            -- 変更前：PROFICIENCY_SUBCLASS_MST
            -- 変更後：PROFICIENCY_SUBCLASS_YDAT
            -- proficiency_subclass_ydat_rev1.1.sql

2012/05/30  1.テーブル名変更
              - COURSE_GROUP_DAT → COURSE_GROUP_CD_DAT
              - COURSE_GROUP_HDAT → COURSE_GROUP_CD_HDAT
              
2012/06/26  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2012/07/11  1.合計点および学期成績の算出式を変更

2012/07/17  1.PROFICIENCY_DATから学籍番号を抽出する際、受講名簿を参照するように修正

2014/08/22  1.style指定修正。レイアウト修正

2015/02/09  1.科目コンボは単位マスタ、コースグループを参照して表示するよう修正

2015/02/10  1.科目コンボの内容が表示されない不具合修正

2015/02/13  1.科目コンボの内容の仕様変更
            -- 指定学年のコースグループの課程学科の生徒が受けた試験科目を表示する。
            -- 試験科目とは、成績データにある科目
