-- kanji=漢字
-- $Id: certif_issue_eachtype_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CERTIF_ISSUE_EACHTYPE_DAT

create table CERTIF_ISSUE_EACHTYPE_DAT \
    (YEAR           varchar(4)    not null, \
     CERTIF_INDEX   varchar(5)    not null, \
     SCHREGNO       varchar(8), \
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

alter table CERTIF_ISSUE_EACHTYPE_DAT add constraint PK_CERTISSUE_EACH primary key \
    (YEAR, CERTIF_INDEX)


