# kanji=漢字
# $Id: readme.txt 69787 2019-09-18 07:11:31Z ishii $

2009/10/28  1.新規作成(処理：東京KNJI024　レイアウト：東京KNJI024参考)

2009/12/07  1.KNJI024の処理と同一にした。

2009/12/08  1.年度開始終了日付は学期マスタからではなく固定で04/01～03/31に修正

2010/04/01  1.「BASE_MST」にフィールド追加、「GUARANTOR_ADDRESS_DAT」「GUARDIAN_ADDRESS_DAT」追加

2010/08/04  1.住所フラグ(*ADDR_FLG)追加に伴う修正

2010/08/06  1.「HEXAM_ENTREMARK_DAT」にフィールド追加に伴う修正

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

2012/01/25  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。

2012/01/30  1.「HEXAM_ENTREMARK_DAT」のフィールド追加に伴う修正をした。

2012/02/02  1.「GRD_BASE_MST」のCUR_ADDR_FLG更新内容修正
                - NULL ⇒ 「SCHREG_ADDRESS_DAT」のADDR_FLG

2012/06/14  1.GUARDIAN_DAT.GUARANTOR_REAL_NAME、GUARANTOR_REAL_KANA、GUARANTOR_ADDR_FLGを追加

2013/12/06  1.住所のサイズ変更等に伴う修正
            -- rep-freshman_dat_rev1.15.sql
            -- rep-guardian_dat_rev1.4.sql
            -- rep-guardian_address_dat_rev1.5.sql
            -- rep-schreg_address_dat_rev1.9.sql
            -- rep-schreg_base_mst_rev1.9.sql
            -- rep-schreg_ent_grd_hist_dat_rev1.4.sql
            -- rep-guarantor_address_dat_rev1.4.sql
            -- rep-grd_address_dat_rev1.6.sql
            -- rep-grd_guardian_dat_rev1.4.sql
            -- rep-grd_guardian_address_dat_rev1.3.sql
            -- rep-grd_guarantor_address_dat_rev1.4.sql
            -- rep-grd_base_mst_rev1.15.sql

2013/12/25  1.プロパティー「useAddrField2」を追加した。
            -- '1'がセットされている場合は追加分の住所2を使用する。

2014/05/26  1.更新/削除等のログ取得機能を追加

2014/09/02  1.保護者情報２テーブルの追加に伴う修正
            -- guardian2_address_dat_rev1.1.sql
            -- guardian2_dat_rev1.1.sql
            -- grd_guardian2_address_dat_rev1.1.sql
            -- grd_guardian2_dat_rev1.1.sql
            ※ プロパティ（useGuardian2 = 1）の時、使用する

2015/03/30  1.GRD_BASE_MSTのENT_ADDR2、GRD_ADDR2はテーブルにフィールドがある場合に移行する

2016/03/26  1.DB2の8.1でDBエラーになる不具合を修正

2016/09/21  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/09/21  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/09/07  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2018/07/26  1.GRD_HEXAM_ENTREMARK_DAT移行にTOTALSTUDYACT_SLASH_FLG, TOTALSTUDYVAL_SLASH_FLG, ATTENDREC_REMARK_SLASH_FLGを追加
            2.GRD_HEXAM_ENTREMARK_HDAT移行にTOTALSTUDYACT_SLASH_FLG, TOTALSTUDYVAL_SLASH_FLGを追加
            3.GRD_HEXAM_ENTREMARK_HDAT移行にBEHAVEREC_REMARK,HEALTHREC,SPECIALACTREC,TRIN_REFを追加

2019/02/18  1.駿台甲府は、GRD_BASE_MST.GRD_TERMはSCHREG_BASE_MSTからコピーする。（駿台以外は算出した値をセット）

2019/08/05  1.フィールド追加に伴うDBエラーの修正

2019/09/18  1.リファクタリング
            2.SQL修正
