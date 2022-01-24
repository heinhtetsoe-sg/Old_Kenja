-- kanji=漢字
-- $Id: schreg_details_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHREG_DETAILS_DAT

create table SCHREG_DETAILS_DAT \
(  \
    SCHREGNO                varchar(8) not null, \
    REQUIRED_FLG            varchar(1), \
    NOT_REQUIRED_CLASSCD    varchar(2), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_DETAILS_DAT  \
add constraint PK_SCHREG_D_DAT \
primary key  \
(SCHREGNO)
