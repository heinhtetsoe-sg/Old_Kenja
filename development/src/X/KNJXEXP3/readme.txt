# kanji=漢字
# $Id: readme.txt 56591 2017-10-22 13:04:39Z maeshiro $

2009/03/19  1.tokio:/usr/local/development/src/X/KNJXEXP3からコピーした。

2009/03/26  1.knjh160a追加に伴う修正

2012/10/23  1.プロパティーファイルにKNJXEXP_SEARCHを追加した。
            -- ソート指定出来るよう修正、KNJXEXP_SEARCH=SCHREGNO 学籍番号順
               今のところ学籍番号順以外は、出席番号順としている。
               
2013/04/10  1.プロパティKNJXEXP_SEARCH=SCHREGNO の時、入学年度+学籍番号順に修正

2013/04/11  1.プロパティKNJXEXP_SEARCH=SCHREGNO の時、学籍番号上4桁降順、下4桁昇順に修正
            2.修正

2016/06/16  1.校種条件追加
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/03/15  1.校種条件追加

2017/04/10  1.KNJH160AでプロパティKNJH010A_DISASTER_P=1 の時、環はKNJH010A_DISASTERを呼ぶ

2017/04/14  1.KNJH160AでプロパティKNJH010A_DISASTER_校種=1 の時、環はKNJH010A_DISASTERを呼ぶ、に変更

2017/09/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
