-- kanji=漢字
-- $Id: certif_detail_eachtype_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CERTIF_DETAIL_EACHTYPE_DAT

create table CERTIF_DETAIL_EACHTYPE_DAT \
    (YEAR           varchar(4)    not null, \
     CERTIF_INDEX   varchar(5)    not null, \
     SCHREGNO       varchar(8), \
     TYPE           varchar(1), \
     REMARK1        varchar(60), \
     REMARK2        varchar(60), \
     REMARK3        varchar(60), \
     REMARK4        varchar(60), \
     REMARK5        varchar(60), \
     REMARK6        varchar(60), \
     REMARK7        varchar(60), \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CERTIF_DETAIL_EACHTYPE_DAT add constraint PK_CERTIF_DETAIL primary key \
    (YEAR, CERTIF_INDEX)


