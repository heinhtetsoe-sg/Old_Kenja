-- kanji=漢字
-- $Id: 199aa1a8a5aa1f536b8d3fd9afaa7672af0226c2 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table SCHREG_ENT_GRD_HIST_DAT

create table SCHREG_ENT_GRD_HIST_DAT \
(  \
    SCHREGNO                    varchar(8)    not null, \
    SCHOOL_KIND                 varchar(2)    not null, \
    FINSCHOOLCD                 varchar(12), \
    FINISH_DATE                 date, \
    CURRICULUM_YEAR             varchar(4), \
    ENT_DATE                    date, \
    ENT_DIV                     varchar(1), \
    ENT_REASON                  varchar(75), \
    ENT_SCHOOL                  varchar(75), \
    ENT_ADDR                    varchar(150), \
    ENT_ADDR2                   varchar(150), \
    GRD_DATE                    date, \
    GRD_DIV                     varchar(1), \
    GRD_REASON                  varchar(75), \
    GRD_SCHOOL                  varchar(75), \
    GRD_ADDR                    varchar(150), \
    GRD_ADDR2                   varchar(150), \
    GRD_NO                      varchar(8), \
    GRD_TERM                    varchar(4), \
    TENGAKU_SAKI_ZENJITU        date, \
    TENGAKU_SAKI_GRADE          VARCHAR(60)  , \
    NYUGAKUMAE_SYUSSIN_JOUHOU   varchar(768), \
    REGISTERCD                  varchar(10), \
    UPDATED                     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_ENT_GRD_HIST_DAT add constraint PK_ENT_GRD_HIST primary key \
    (SCHREGNO, SCHOOL_KIND)
