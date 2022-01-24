-- kanji=漢字
-- $Id: 1aa6cb41d5e7f3f5c70b7f699df73e34a7275941 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ

ALTER TABLE COLLECT_SLIP_PLAN_PAID_DAT ADD COLUMN SGL_OUTPUT_FLG VARCHAR(1)

reorg table COLLECT_SLIP_PLAN_PAID_DAT

