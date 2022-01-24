# kanji=漢字
# $Id: readme.txt 56588 2017-10-22 12:57:09Z maeshiro $

KNJH543A
実力科目合併先計算方法設定
-----
学校：明治学園
-----
更新テーブル
・PROFICIENCY_COMB_GCALC_DAT
参照テーブル
・PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT
・PROFICIENCY_SUBCLASS_MST
・PROFICIENCY_YMST
・PROFICIENCY_MST

2012/04/04  1.KNJZ219を元に新規作成

2012/05/30  1.テーブル名変更
              - COURSE_GROUP_HDAT → COURSE_GROUP_CD_HDAT
              
2012/06/27  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
                
2012/07/24  1.教育課程に伴う修正漏れ対応（科目コード表示）

2013/09/04  1.教育課程対応不必要の為、修正カット

2014/11/04  1.ログ取得機能追加

2016/09/19  1.学年コンボ修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/10  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
