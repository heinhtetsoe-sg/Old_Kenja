# kanji=漢字
# $Id: readme.txt 67735 2019-05-31 05:30:59Z yamauchi $

2012/03/05  1.新規作成

2012/03/12  1.学年コンボを切り換えた時に表示されている項目をNULLにする

2012/05/30  1.テーブル名変更
              - COURSE_GROUP_DAT → COURSE_GROUP_CD_DAT
              - COURSE_GROUP_HDAT → COURSE_GROUP_CD_HDAT

2014/12/16  1.style指定修正

2015/02/25  1.リストTOリストのIE10以上の対応
                - IE10以上ではnameタグはdocument.getElementsByID()で取得不可能のため、document.forms[0]でnameタグを取得

2016/09/21  1.左画面の設定一覧、学年コンボ、コピー処理、更新処理修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDの参照


2019/05/31  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
