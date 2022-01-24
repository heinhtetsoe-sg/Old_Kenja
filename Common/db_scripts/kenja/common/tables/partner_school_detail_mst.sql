-- kanji=漢字
-- $Id: 0d8797f44c0600fd79b98a56db53a61c1e602159 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table PARTNER_SCHOOL_DETAIL_MST

create table PARTNER_SCHOOL_DETAIL_MST \
(  \
    PARTNER_SCHOOLCD    varchar(12)  not null, \
    PARTNER_SCHOOL_SEQ  varchar(3)   not null, \
    REMARK1             varchar(90), \
    REMARK2             varchar(90), \
    REMARK3             varchar(90), \
    REMARK4             varchar(90), \
    REMARK5             varchar(90), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PARTNER_SCHOOL_DETAIL_MST add constraint PK_PRTNSCHOOL_DT_M \
primary key (PARTNER_SCHOOLCD, PARTNER_SCHOOL_SEQ)
