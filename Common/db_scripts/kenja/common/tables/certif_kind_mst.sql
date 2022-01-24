-- kanji=漢字
-- $Id: 680c5052cbda57e62ad7ce517ad8de07f743050b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table CERTIF_KIND_MST

create table CERTIF_KIND_MST \
      (CERTIF_KINDCD      varchar(3)      not null, \
       KINDNAME           varchar(24), \
       ISSUECD            varchar(1), \
       STUDENTCD          varchar(1), \
       GRADUATECD         varchar(1), \
       DROPOUTCD          varchar(1), \
       ELAPSED_YEARS      SMALLINT, \
       CERTIF_DIV         varchar(2), \
       CERTIF_GRPCD       varchar(3), \
       CURRENT_PRICE      varchar(4), \
       GRADUATED_PRICE    varchar(4), \
       ISSUENO_AUTOFLG    varchar(1), \
       CERTIF_SCHOOL_KIND varchar(2), \
       REGISTERCD         varchar(10), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CERTIF_KIND_MST add constraint PK_CERTKIND_MST primary key \
      (CERTIF_KINDCD)


