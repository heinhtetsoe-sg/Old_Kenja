-- kanji=漢字
-- $Id: rep-certif_issue_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CERTIF_ISSUE_DAT_OLD
create table CERTIF_ISSUE_DAT_OLD like CERTIF_ISSUE_DAT
insert into  CERTIF_ISSUE_DAT_OLD select * from CERTIF_ISSUE_DAT

drop table CERTIF_ISSUE_DAT

create table CERTIF_ISSUE_DAT \
    (YEAR           varchar(4)    not null, \
     CERTIF_INDEX   varchar(5)    not null, \
     SCHREGNO       varchar(8), \
     APPLICANTNO    varchar(7), \
     TYPE           varchar(1), \
     CERTIF_KINDCD  varchar(3), \
     GRADUATE_FLG   varchar(1), \
     APPLYDATE      date, \
     ISSUERNAME     varchar(40), \
     ISSUECD        varchar(1), \
     CERTIF_NO      integer, \
     ISSUEDATE      date, \
     CHARGE         varchar(1), \
     PRINTCD        varchar(1), \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CERTIF_ISSUE_DAT add constraint PK_CERTISSUE_EACH primary key \
    (YEAR, CERTIF_INDEX)

insert into  CERTIF_ISSUE_DAT \
(select \
    YEAR, \
    CERTIF_INDEX, \
    SCHREGNO, \
    CAST(NULL AS varchar(7)), \
    TYPE, \
    CERTIF_KINDCD, \
    GRADUATE_FLG, \
    APPLYDATE, \
    ISSUERNAME, \
    ISSUECD, \
    CERTIF_NO, \
    ISSUEDATE, \
    CHARGE, \
    PRINTCD, \
    REGISTERCD, \
    UPDATED \
 from CERTIF_ISSUE_DAT_OLD)

