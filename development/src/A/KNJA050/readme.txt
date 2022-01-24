# kanji=漢字
# $Id: readme.txt 75967 2020-08-12 06:32:03Z yamashiro $
/*** KNJA050-年度確定処理 readme.txt***/

2005/03/31 devel-utf8よりコピー
2005/04/06 近大-作業依頼書20041224-01｢卒業生台帳番号の採番を行う、行わないを選択実行可能に｣対応
        ・卒業生台帳番号処理モード選択コンボ追加
        ・年度確定処理内容変更、年度確定処理実行時、台帳番号処理は行わない。
        ・各チェック処理変更修正
        ・年度確定処理後に不用卒業台帳番号をクリア
        ・SCHREG_BASE_MST(学籍基礎マスタ)テーブル定義対応、項目削除対応(本籍郵便番号,本籍住所１,本籍住所２ )
        ・GRD_TERM(卒業期)の更新内容修正
2005/07/11 納品
2005/10/19 学籍基礎・住所の変更に伴う修正。alp m-yama
2005/12/18 CONTRL_MSTの更新を行わないよう修正。alp m-yama
2006/07/05 NO001 m-yama   SCHREG_REGD_DATの次年度作成処理のANNUAL更新時のバグ修正。


/**
 * 以下よりCVS管理
 */
2006/07/06  保険関連テーブル変更に伴う修正。
2006/07/07  grd_htrainremark_datのフィールド追加に伴う修正をした。
2006/07/10  grd_medexam_tooth_datのフィールド追加に伴う修正をした。

2007/04/03  更新前チェックを追加した。
            SCHREG_STUDYREC_DATのフィールド追加に伴う修正をした。

2007/04/12  新入生データをベースに移行時、60Byteのエリアを30Byteのエリアに書込んでいた不正処理を修正した。

2008/03/27  1.コメント化された処理を削除
            2.WHERE句の共通部分をメソッドにした
            3.Delete→Insertに変更

2008/03/30  1.処理学年コンボを追加。
            2.チェック処理を学年毎に行うように変更した
            3.更新処理を学年毎に行うように変更した

2009/03/26  1.テーブル変更に伴う修正。組名称1、組名称2、年名称を追加

2009/03/27  1.年度確定処理時に学籍基礎履歴も更新するように修正

2009/06/30  1.下記のテーブル変更に伴う修正
            -- rep-grd_medexam_tooth_dat.sql
            -- rep-grd_medexam_det_dat.sql
            -- rep-grd_medexam_hdat.sql
            -- rep-medexam_det_dat.sql
            -- rep-medexam_hdat.sql
            -- rep-medexam_tooth_dat.sql

2009/07/22  1.HEXAM_ENTREMARK_DAT変更に伴う修正

2009/07/24  1.PHPのヴァージョンUPに伴う修正
            -- mktime()
               の仕様がPHP5から変わったようです。
               日付指定がブランクの場合エラーを返すようになった。

2009/08/27  1.GRD_BASE_MSTの変更に伴う修正

2009/09/25  1.TRAIN_REF1フィールド、追加に伴う修正
            -- rep-hexam_entremark_dat.sql-1.4
            -- rep-grd_hexam_entremark_dat.sql-1.2

2009/10/28  1.フィールド追加に伴う修正
            -- フィールド: PRINT_FLG
            -- rep-schreg_studyrec_dat.sql-1.4
            -- rep-grd_studyrec_dat.sql-1.2
            -- フィールド: CLASSACT, STUDENTACT, CLUBACT, SCHOOLEVENT
            -- rep-htrainremark_dat.sql-1.4
            -- rep-grd_htrainremark_dat.sql-1.5

2009/12/08  1.「卒業生卒業日付」「新入生入学日付」のチェック範囲を学期マスタの値から04/01～03/31に修正

2009/12/25  1.台帳番号取得順指定追加

2010/03/29  1.SQLエラーの修正
            2.日付の形式の修正

2010/03/31  1.以下の不具合を修正
            -- 年度確定処理ボタン押し下げ後、ポップアップ画面がエラー（正常に表示されない）
            -- ポップアップ画面のパス指定が間違っていたのを修正した

2010/04/01  1.「BASE_MST」にフィールド追加、「GUARANTOR_ADDRESS_DAT」「GUARDIAN_ADDRESS_DAT」追加

2010/04/05  1.以下の通り修正
            -- SCHREG_REGD_DATがある時は、ログイン年度のSCHREG_REAGD_DATの年次＋1　をセットする。
            -- SCHREG_REGD_DATがない時は、
            --   名称マスタ「Z010」「00」のNAMESPARE2=not　nullのときは、
            --       学年(grade)をセットする。
            --   それ以外は、
            --       0＋1をセットする。
            2.学籍学歴テーブル追加。
            -- schreg_ent_grd_hist_dat.sql-1.1
            -- grd_medexam_det_datの修正漏れを直した

2010/04/07  1.前過程卒業日を追加、卒業日がなければ前過程卒業日を使用する。

2010/04/28  1.「grd_hexam_entremark_dat」に「TOTALSTUDYACT」「TOTALSTUDYVAL」を追加

2010/08/04  1.住所フラグ(*ADDR_FLG)追加に伴う修正

2010/12/17  1.FRESHMAN_DATのテーブル変更（３項目追加）に伴い修正。
            -- SCHREG_ADDRESS_DATの「住所2出力フラグ、住所開始日、住所終了日」に
            -- FRESHMAN_DATの「住所2出力フラグ、住所開始日、住所終了日」をセットする。
            -- 但し、「住所開始日、住所終了日」がない場合は、現状通りの日付をセットする。
            -- rep-freshman_dat_rev1.9.sql

2011/02/03  1.TRANSLATE_KANAを使用して、かな氏名ソートする。

2011/02/15  1.以下の不具合を修正。
            -- 処理学年コンボで卒業生を選択したとき、
            -- 卒業生卒業日付の初期値に、学校マスタの入学日付が表示されていた。
            -- 卒業生卒業日付の初期値は、学校マスタの卒業日付を表示するようにした。

2011/02/15  1.誤字修正
            2.SQL修正

2011/04/01  1.卒業生データ移行のSQLを修正。フィールド追加。
            -- GRD_GUARDIAN_DAT
            --     GUARD_REAL_NAME,
            --     GUARD_REAL_KANA,
            -- GRD_MEDEXAM_DET_DAT
            --     URI_ADVISECD,
            --     MANAGEMENT_REMARK,
            --     OTHER_ADVISECD,
            --     OTHER_REMARK,
            -- GRD_MEDEXAM_TOOTH_DAT
            --     CALCULUS,
            --     CHECKADULTTOOTH,
            --     DENTISTTREATCD,

2011/04/07  1.SQL修正（処理時間短縮）

2011/04/14  1.学籍学歴データ処理修正

2011/04/20  1.学籍学歴データ処理修正

2011/04/21  1.学籍学歴データ処理修正
            2.学籍学歴データ処理修正

2011/04/24  1.OLD_GRADEが無くても対象とする。
            2.学籍学歴データ処理修正

2011/04/25  1.REMAINGRADE_FLG != '1'(留年以外)を対象とする。

2011/04/26  1.学籍学歴データ処理修正

2011/04/29  1.卒業生テーブルへの更新処理をカット

2011/08/10  1.Insert 時に入学年度（CURRICULUM_YEAR）を追加
              - ENT_DATE の Year 部分
              - 1～3月までのときは Year - 1

2011/09/06  1.以下の通り修正。
            -- 前回の修正をカットし、
            -- FRESHMAN_DATの課程入学年度（CURRICULUM_YEAR）をセットする。
            -- rep-freshman_dat_rev1.10.sql

2011/09/07  1.課程入学年度の追加処理を修正。
            -- ・FRESHMAN_DATがない生徒 ===> 入学日付より課程入学年度を算出。
            -- ・FRESHMAN_DATがある生徒 ===> FRESHMAN_DATの課程入学年度（CURRICULUM_YEAR）をセット。

2012/03/19  1.FRESHMAN_DATの項目「受験番号」追加に伴い修正
            -- SCHREG_BASE_DETAIL_MSTのBASE_SEQ=003のBASE_REMARK1へ登録
            -- rep-freshman_dat_rev1.11.sql

2012/03/20  1.FRESHMAN_DATの項目「地区コード(AREACD)」追加に伴い修正
            -- rep-freshman_dat_rev1.11.sql

2012/03/29  1.2010/04/05の修正により、チェック処理が動いていなかったので修正

2012/04/17  1.重複エラーの対応
            -- 学籍在籍データ

2012/05/16  1.学籍学歴データ作成の対象を01,04固定にしていたが
              名称マスタA023のNAME2を使用する。

2013/04/12  1.新入生の学年チェックを可変に変更(A023のNAME2)

2013/04/15  1.卒業生の採番処理の指定順に取得できない不具合のとりあえずの対応

2013/04/16  1.新入生の学年チェックを可変に変更(A023のNAME2)

2013/07/11  1.校種が変わる場合は、住所データをコピーする。

2013/12/06  1.住所のサイズ変更等に伴う修正
            -- rep-freshman_dat_rev1.15.sql
            -- rep-guardian_dat_rev1.4.sql
            -- rep-guardian_address_dat_rev1.5.sql
            -- rep-schreg_address_dat_rev1.9.sql
            -- rep-schreg_base_mst_rev1.9.sql
            -- rep-schreg_ent_grd_hist_dat_rev1.4.sql
            -- rep-guarantor_address_dat_rev1.4.sql

2013/12/25  1.プロパティー「useAddrField2」を追加した。
            -- '1'がセットされている場合は追加分の住所2を使用する。

2014/04/02  1.SCHREG_REGD_DATの削除条件変更。
            -- CLASS_FORMATION_DATに存在 → 指定学年全て

2014/04/03  1.近大はTRANSLATE_KANAを使用しない

2014/05/26  1.更新/削除等のログ取得機能を追加

2015/02/23  1.SCHREG_NAME_SETUP_DATのDIV='06'で登録されている生徒はNAME_KANAではなくREAL_NAME_KANAを参照し、ソートする

2015/06/04  1.JavascriptのgetYear()メソッドをgetFullYear()メソッドに変更

2015/12/18  1.PHP5.6 Verup対応
                - 文字列offset問題

2016/03/31  1.氏名表示用に余分な空白が入るのを修正

2016/08/22  1.学年コンボの参照先にSCHREG_REGD_GDATを追加

2016/09/21  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/12/28  1.SCHREG_ADDRESS_DAT、GUARDIAN_ADDRESS_DATのフィールド（電話番号２）追加に伴う修正

2017/04/18  1.2016/12/28の修正漏れ

2017/04/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
            2.単位制かつプロパティー「useKeepGrade」が"1"かつプロパティー「useKeepAnnual」が"1"のとき
            -- SCHREG_REGD_DATのANNUALをセット

2017/08/23  1.台帳番号採番処理での卒業期の更新処理修正
            -- 卒業期クリア処理をしない(プロパティー：grdTermSetNyuugaku=1の時)
            -- 学校マスタの参照方法変更(校種ありの場合に対応)
            -- 卒業期をセットしない(プロパティー：grdTermSetNyuugaku=1の時)
            2.新入(A023.NAME2)の更新でgrdTermSetNyuugaku=1の時、卒業期をセットする。

2017/08/28  1.卒業生選択時に、卒業期のクリア処理をカット
            2.grdTermSetNyuugaku=1以外の時は、クリアする。

2018/04/03  1.台帳番号採番処理を修正
            -- 卒業区分「1:卒業」に加えて、名称マスタ「A003」の予備1が"1"の区分も対象
            2.卒業生等にプロパティーによる校種の制御処理を追加

2018/04/09  1.次年度以降の住所があれば、住所作成はしない。

2018/04/16  1.前回の不具合対応

2018/04/18  1.卒業生以外で処理年度と次年度の校種が違うとき、学籍基礎マスタを更新に変更

2018/07/04  1.プロパティーによる校種の制御処理の修正漏れ

2018/08/01  1.SCHREG_ADDRESS_DAT.TELNO_MEMO、TELNO2_MEMO追加にともなうDBエラー対応

2019/01/24  1.来年度の人数、卒業生人数の条件に校種指定を追加

2019/03/14  1.台帳番号採番処理で校種選択可にした。
            2.卒業生台帳番号のデフォルト値を修正
            3.プロパティー：useKnja050_select_schoolKind = 1の時校種選択可
            4.校種は全てをカットして、ブランクを追加。選択必須とする。

2019/03/29  1.台帳番号裁判済チェックの校種指定を追加

2019/04/19  1.2018/04/18の修正の記述ミス

2019/08/15  1.2019/01/24の修正不具合対応

2019/12/06  1.SCHREG_REGD_DATの削除処理変更
            -- CLASS_FORMATION_DATの対象生徒の在籍データも削除
            2.保護者住所データ、保証人住所データの削除処理追加

2020/04/23  1.台帳番号採番処理の順番に以下のパターンを追加 (※駒沢専用)
            --「総合成績順 + 年組番順」

2020/08/12  1.最終学期でのログインのみ有効のメッセージの統一。MSG300 → MSG311
            -- View.php(rev.75964)

2020/11/20  1.コード自動整形
            2.台帳番号採番処理の台帳番号をNULLにする対象者の条件を修正
            修正前：
            ・除籍区分が'1'ではない生徒
              かつ
            ・名称マスタ「A003」の予備1が'1'の除籍区分ではない生徒
              または
            ・仮卒業フラグ(KNJA032で設定)が'1'ではない生徒
            修正後：
            ・除籍区分が'1'ではない生徒
              かつ
            ・名称マスタ「A003」の予備1が'1'の除籍区分ではない生徒
              かつ
            ・仮卒業フラグ(KNJA032で設定)が'1'ではない生徒

2021/02/02  1.FRESHMAN_DATからSCHREG_BASE_MSTに塾コード(PRISCHOOLCD)を移行するよう修正

2021/03/31  1.SCHREG_ADDRESS_DATのinsertに列指定を追加 (京都府DBエラー対応）

2021/04/08  1.単位制 かつ 処理年度と次年度の校種が同じとき、
            -- SCHREG_ENT_GRD_HIST_DAT の delete & insert は行わない。

2021/04/09  1.プロパティ「grdNo_DaichouNoOrder」にソート順がセットされている場合、以下の対応を行うよう修正
            -- 卒業生台帳番号の連番ラジオボタンの下にプロパティから取得したソート用のラジオボタン（ラベルはソートに使用する項目名）を追加するよう修正
            -- 台帳番号採番処理のソート処理の修正
