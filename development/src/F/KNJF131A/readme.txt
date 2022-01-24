# kanji=漢字
# $Id: readme.txt 64929 2019-01-18 06:47:55Z matsushima $

2009/11/06  1.KNJF130Aを元に新規作成
            2.学年は SCHREG_REGD_GDAT の 「SCHOOL_KIND」が H、J の学年を出力するよう修正

2009/11/09  1.以下を修正
            -- 日付の上に「利用期間」を追加
            -- 「利用期間」は学期マスタではなく、固定で「04/01」～「03/31」に修正 
            2.「利用区分」を追加、CSVのレイアウトを knjf121a にあわせた。
            3.帳票にパラメーターがわたらないバグ修正、ソート順の修正

2010/03/03  1.MAX学期の学年を参照するよう修正した。

2010/03/03  1.曜日がずれるバグを修正した。

2010/04/22  1.データがないときのエラー修正

2010/07/01  1.文言変更 健康相談活動 → 健康相談

2014/08/05  1.ログ取得機能追加

2016/09/16  1.画面の表左上とCSVの1行目にSCHOOL_MST.SCHOOLNAME2を表示
            -- プロパティー「useSchool_KindField」とSCHOOLKINDで表示切替
            2.利用区分リストとCSVの固定文言「生徒」修正
            -- SETTING_DAT参照、なければ固定で「生徒」
            -- プロパティー「useSchool_KindField」とSCHOOLKINDで表示切替
            3.CSVの学年修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2016/09/20  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/10/06  1.「生徒」名称取得を修正

2017/05/22  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/14  1.前回の修正漏れ

2019/01/18  1.CSV出力の文字化け修正(Edge対応)