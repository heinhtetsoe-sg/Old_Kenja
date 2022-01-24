-- $Id: 6caf67d9b619befe516abe4cc0f4c025ec4d5175 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table RELATIVEASSESS_RATE_MST

create table RELATIVEASSESS_RATE_MST( \
     YEAR           VARCHAR(4)   NOT NULL, \
     ASSESSLEVEL    SMALLINT     NOT NULL, \
     ASSESSRATE     SMALLINT     , \
     REGISTERCD     VARCHAR(10), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table RELATIVEASSESS_RATE_MST add constraint pk_relaas_rate_ms primary key \
    (YEAR, ASSESSLEVEL)
