# kanji=漢字
# $Id: readme.txt 71503 2019-12-26 13:36:30Z maeshiro $

2013/03/13  1.KNJC162を元に新規作成

2013/03/15  1.出欠端数処理の引数にuseCurriculumcdの指定がなかった不具合を修正
            2.サブタイトル追加

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加

2014/05/29  1.更新/削除等のログ取得機能を追加

2014/08/27  1.style指定修正

2016/06/16  1.プロパティーuse_SchregNo_hyoji追加

2016/08/05  1.プロパティーuseTestCountflg追加

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2019/02/12  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。

2019/06/25  1.プロパティーknjc163useAttendSemesDat追加
            2.プロパティーknjc163useAttendSemesDatが1の場合、CSV出力用は累積データを対象に含める

2019/07/12  1.CSVにて、生徒氏名（英語・日本語）切替処理追加
            2.プロパティ「use_prg_schoolkind」が1の時、年組コンボ取得

2019/12/26  1.プロパティーhibiNyuuryokuNasiが1の場合、CSV出力用は累積データを対象に含める
            2.CSV出力をservletに変更

2021/04/20  1.コード自動整形
            2.csvのリクエスト処理をPHPから投げるよう変更