# kanji=漢字
# $Id: readme.txt 64926 2019-01-18 06:46:58Z matsushima $

2009/08/28  1.KNJF120を元に新規作成

2009/10/07  1.以下を修正した
            -- 1件もなくても出力するよう修正
            -- 横の合計を追加

2010/03/02  1.健康相談活動、その他、生徒以外を追加した。
            2.名称マスタの「F710」参照をカットした。

2010/03/03  1.学期マスタからの日付範囲参照をカットし、固定に変更した。

2014/08/05  1.ログ取得機能追加

2016/09/12  1.画面の表左上とCSVの1行目にSCHOOL_MST.SCHOOLNAME2を表示
            -- プロパティー「useSchool_KindField」とSCHOOLKINDで表示切替
            2.ＣＳＶ出力の固定文言「生徒」修正
            -- SETTING_DAT参照、なければ固定で「生徒」
            -- プロパティー「useSchool_KindField」とSCHOOLKINDで表示切替
            3.ＣＳＶ出力の母集団を修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2016/10/06  1.「生徒」名称取得を修正

2017/05/22  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/14  1.前回の修正漏れ

2019/01/18  1.CSV出力の文字化け修正(Edge対応)