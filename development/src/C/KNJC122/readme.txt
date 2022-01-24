# kanji=漢字
# $Id: readme.txt 56585 2017-10-22 12:47:53Z maeshiro $

2010/02/15  1.KNJC121を元に新規作成
            2.未入力数チェックボックスをカット
            3.学年コンボは「H」と「J」を表示、上限値の値は「実時数」の時表示するよう修正

2010/06/16  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2010/06/21  1.文字修正「上限値」⇒「上限値警告」
            2.「出欠集計範囲」の開始日付を固定にした。

2010/09/21  1.法定授業とか、実授業とかの区別はなく、以下の通りに変更。
            -- 欠課数上限値（要注意）
            -- 　◎履修　　　　○修得

2010/12/07  1.hiddenに"chikokuHyoujiFlg"を追加した。

2011/05/20  1.SQL修正

2012/05/11  1.プロパティーuseCurriculumcd追加

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加

2014/02/02  1.プロパティーuseTestCountflg追加

2014/05/28  1.更新/削除等のログ取得機能を追加

2014/08/27  1.style指定修正

2016/06/06  1.制限付きの条件に副担任も追加

2016/06/16  1.プロパティーuse_SchregNo_hyoji追加

2016/09/20  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/05/19  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/07/13  1.パラメータ追加use_school_detail_gcm_dat useSchool_KindField SCHOOLCD SCHOOLKIND
