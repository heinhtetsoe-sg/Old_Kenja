# kanji=漢字
# $Id: readme.txt 71439 2019-12-25 05:54:17Z matsushima $

2015/12/02  1.新規作成

2015/12/02  1.文言変更
            2.学級活動のコンボはHRを表示する

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/09/22  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 (部活動、委員会)

2017/04/28  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/15  1.校種コンボ追加「use_prg_schoolkind == "1"」の時表示
            2.前回の修正漏れ

2017/12/29  1.チェックボックス・ラジオボタンの初期値設定機能追加
            -- common.js
            -- Model.php
            -- prg_default_value_dat.sql(rev.57783)

2019/12/25  1.部活動複数校種設定対応
            -- プロパティー「useClubMultiSchoolKind」参照