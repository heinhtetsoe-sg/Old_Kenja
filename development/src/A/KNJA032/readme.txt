# kanji=漢字
# $Id: readme.txt 75967 2020-08-12 06:32:03Z yamashiro $

/*** KNJA032_学年進行処理(賢者) readme.txt***/

2007/03/31  新規作成(処理：東京都KNJA031　レイアウト：東京都KNJA031参考)

2008/03/26 成績テーブルの参照を制御。処理コンボリストの文言を制御。

2010/04/05  1.処理コンボリストの文言を制御。
            -- 名称マスタ「Z010」「00」の「NAMESPARE2 = 1」「GRADE = 3 or 16」の場合、「1:進級・卒業」と表記
            -- 名称マスタ「Z010」「00」の「NAMESPARE2 = 2」「GRADE = 3」の場合、「1:進級・終了」と表記


2010/04/06  1.処理コンボリストの文言を制御。
            -- 中高一貫の６学年（高３）は、「卒業」を表示する
            -- 中高一貫の６学年（高３）は、「進級」を表示しない
            2.「進級」処理のCLASS_FORMATION_DATのGRADEについて
            -- 中高一貫の１６学年（小６）は、「固定値：01」をセット
            3.処理コンボリストの文言を制御。
            -- 中高一貫の３学年（中３）と１６学年（小６）は、「卒業」を表示する

2011/02/25  1.SCHREG_ENT_GRD_HIST_DATの更新処理追加
            2.処理コンボの表示内容を、中高一貫や学年固定で判断せずに
              A023を使用して表示するよう修正

2011/03/30  1.卒業表示学年で、From～ToのToより小さい学年の場合は、進級も表示する。

2012/01/25  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。

2012/03/30  1.文言変更。終了 → 修了

2012/04/05  1.「2:卒業」を追加

2013/03/01  1.近大の処理を追加

2014/05/26  1.更新/削除等のログ取得機能を追加

2014/05/27  1.ログ取得機能修正 (画面表示データを取得するよう修正)

2015/02/24  1.useTestCountflg = TESTITEM_MST_COUNTFLG_NEW_SDIVの時、RECORD_SCORE_DAT.SCORE_DIV='09'参照

2015/03/26  1.除籍区分を修正

2015/12/04  1.修得単位のSQLを学年制と単位制で共通にした。
            2.卒業可能学年でなければSCHREG_STUDYREC_DATを含まない。

2016/09/20  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/04/24  1.処理コンボに学校区分参照追加
            -- 学年制は従来の通り
            -- 単位制は進級、取消は常に表示。卒業可能学年のとき卒業を表示。
            2.進級で単位制かつプロパティー「useKeepGrade」が"1"のとき、新学年に旧学年をセット

2017/04/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/13  1.単位表示は高校のみ

2018/01/15  1.プロパティーuseSchool_KindField='1'の場合、学校マスタの校種指定追加

2018/02/23  1.プロパティーuseSchool_KindField='1'の場合、学校マスタの校種指定追加(前回の修正漏れ対応)

2018/04/02  1.事前チェック追加
            -- 名称マスタ「A023」略称2の存在チェック
            -- 名称マスタ「A003」5:修了の存在チェック
            2.次年度の学年取得等を進行順参照に変更
            -- 名称マスタ「A023」略称2
            3.処理コンボに「4:修了」追加
            -- 名称マスタ「Z010」中高一貫が"2"　かつ
            -- プロパティー「use_prg_schoolkind」が"1"以外　かつ
            -- プロパティー「useSchool_KindField」が"1"　かつ
            -- 名称マスタ「A023」略称2がMAXの校種以外　かつ
            -- 卒業可能学年のとき表示

2018/04/04  1.A023が1件の場合は、ABBV2を1として扱う
            2.修正漏れ

2019/03/14  1.進級終了を選択した場合、SCHREG_BASE_YEAR_DETAIL_MST.SEQ = '007'のBASE_REMARK1に【1】設定
            -- BASE_REMARK1 = 1の場合に、KNJA050で卒業生番号付番対象とする。

2020/08/12  1.最終学期でのログインのみ有効のメッセージの統一。MSG300 → MSG311
            -- View.php(rev.75964)
