# kanji=漢字
# $Id: readme.txt 64928 2019-01-18 06:47:38Z matsushima $

2009/08/27  1.KNJF130を元に新規作成
            2.データが無くても、タイトルのみ表示する。

2010/03/03  1.学期マスタからの日付範囲参照をカットし、固定に変更した。
            2.MAX学期の学年を参照するよう修正した。

2014/08/05  1.ログ取得機能追加

2016/09/12  1.画面の表左上とCSVの1行目にSCHOOL_MST.SCHOOLNAME2を表示
            -- プロパティー「useSchool_KindField」とSCHOOLKINDで表示切替
            2.ＣＳＶ出力の母集団を修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照
            3.学年はSCHREG_REGD_GDATの学年名称1を出力に変更

2017/05/22  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/14  1.前回の修正漏れ

2019/01/18  1.CSV出力の文字化け修正(Edge対応)