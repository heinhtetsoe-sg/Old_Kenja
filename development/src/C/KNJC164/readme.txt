# kanji=漢字
# $Id: readme.txt 68696 2019-07-12 07:30:04Z ishii $

2013/03/15  1.KNJC163を元に新規作成
            2.科目出欠SQLの引数の不具合を修正

2013/08/14  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail、useKoudome追加

2013/08/22  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2014/02/02  1. パラメータuseTestCountflgによってテスト項目マスタのテーブルを変更するように修正
              -- TESTITEM_MST_COUNTFLG_NEW_SDIV 使用はSCORE_DIV = '01'指定
            2.プロパティーuseTestCountflg追加

2014/05/22  1.修正
            -- 勤怠コード27が入力された日付は一日出欠の授業日数にカウントしない
            -- 勤怠コード27が入力された日付校時は科目出欠の授業時数にカウントしない

2014/05/29  1.更新/削除等のログ取得機能を追加

2014/08/27  1.style指定修正

2015/06/04  1.勤怠コード'28'は時間割にカウントしない

2016/06/16  1.プロパティーuse_SchregNo_hyoji追加

2016/08/05  1.プロパティーuseTestCountflg追加

2017/09/25  1.DI_CD(29-32)追加

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2019/05/20  1.校種対応

2019/07/12  1.CSV出力にて、生徒氏名（英語・日本語）切替処理追加

