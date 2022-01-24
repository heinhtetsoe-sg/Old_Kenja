# kanji=漢字
# $Id: readme.txt 60298 2018-05-24 01:07:00Z tawada $

2010/04/13  1.KNJD652を元に新規作成

2010/04/23  1.帳票パラメータ追加
            -- CTRL_YEAR, CTRL_SEMESTER, CTRL_DATE

2012/07/17  1.パラメータuseCurriculumcd追加

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/12/17  1.プロパティーuseKoudome、chikokuHyoujiFlg追加

2014/08/11  1.style指定修正

2015/01/15  1.プロパティーuseTestCountflg追加

2015/09/16  1.学年コンボの表示を SCHREG_REGD_GDAT の GRADE_NAME1 に変更

2016/06/16  1.プロパティーuse_SchregNo_hyoji追加

2016/09/19  1.学年コンボ修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/04/28  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/12/19  1.プロパティーknjd134PrintEachSemesterが1の場合、学期コンボに学年末は非表示、帳票は学期ごとのデータを印字する

2018/05/24  1.プロパティー「useSchool_KindField」or「use_prg_schoolkind」が"1"の時、
            --学年コンボの条件 GRADE < '11' はカット
