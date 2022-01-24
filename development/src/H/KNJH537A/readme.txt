# kanji=漢字
# $Id: readme.txt 56588 2017-10-22 12:57:09Z maeshiro $

2012/04/06  1.新規作成
            2.課程学科コースから課程名称の表示をカットした。

2012/04/11  1.テーブル変更に伴う修正(キー変更)
            -- proficiency_subclass_ydat_rev1.1.sql
            2.注意文を追加

2012/04/12  1.プロパティ「usePerfectCourseGroup = 1」が設定されている場合、ラジオボタンの初期値をコースグループとする

2012/04/13  1.プロパティ「usePerfectCourseGroup = 1」が設定されている場合の対応漏れ、修正

2012/05/30  1.テーブル名変更
              - COURSE_GROUP_HDAT → COURSE_GROUP_CD_HDAT
              
2012/06/27  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2012/07/09  1.考査科目一覧に全科目を表示するように修正した。
            2.リストtoリストのスクロールの不具合を修正した。

2014/08/29  1.ログ取得機能追加
            2.style指定修正
            3.リストtoリストで移動ができない不具合を修正
            
2015/02/25  1.リストTOリストのIE10以上の対応はされていたが、不要な箇所をカット

2016/09/19  1.左画面一覧、学年コンボ、科目リストtoリスト修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/10  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/07/14  1.左フレームで値が変わっても右フレームに渡らない不具合修正
            2.学期コンボでのSEMESTER_MST参照を内部結合(INNER JOIN)に変更

2017/08/28  1.考査科目は一つの実力科目にしか登録できなかったが
              複数実力科目に登録できるよにした
            -- rep-proficiency_subclass_ydat.sql(rev1.2)
