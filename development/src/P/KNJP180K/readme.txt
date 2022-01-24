# kanji=漢字
# $Id: readme.txt 56580 2017-10-22 12:35:29Z maeshiro $

2011/03/26  1.新規作成

2011/05/11  1.テーブル変更に伴う修正
            -- rep-reduction_country_dat_rev1.1.sql
            -- 修正漏れ

2011/06/09  1.基準額の移行、基準/加算の計画移行処理追加

2011/06/27  1.データタイプ指定誤りの修正。

2011/06/28  1.加算時の所得移行での判定を修正
            -- PHPでは、0とNullを同一と扱うので長さで判定する。
            2.前年度のMAXの値で、PLAN_CANCEL_FLGとADD_PLAN_CANCEL_FLGの移行をする。

2011/11/09  1.下記の修正を行った
            -- REDUCTION_AUTHORIZE_DATの加算1を削除、加算2を加算1として更新
            -- REDUCTION_AUTO_CREATE_HIST_DATテーブル追加に伴う修正
            -- reduction_auto_create_hist_dat_rev1.1.sql

2014/10/08  1.REDUCTION_COUNTRY_DAT変更に伴う修正
            -- rep-reduction_country_dat.sql(rev1.3)

2014/10/09  1.2014/10/08以降の修正をキャンセル。

2015/03/28  1.REDUC_RARE_CASE_CD_2 = 'T'の場合は、金額等取得しない。
