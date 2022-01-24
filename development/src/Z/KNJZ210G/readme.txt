# kanji=漢字
# $Id: readme.txt 60419 2018-05-31 03:11:56Z tawada $

2013/04/02  1.KNJZ210Fを元に新規作成

2017/03/07  1.Z009の参照をログイン校種により切替える。

2017/03/23  1.学年コンボにプロパティー「useSchool_KindField」とSCHOOLKINDの参照追加

2018/05/31  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
            --プロパティー「use_prg_schoolkind」OR「useSchool_KindField」が"1"の時、
            --学年コンボの条件 SCHOOL_KIND <> 'H' はカット
