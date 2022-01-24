-- kanji=漢字
-- $Id: 4b41d9594c7d85ee7479c44af34ef66aded3a5ea $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table CERTIF_KIND_YDAT

create table CERTIF_KIND_YDAT \
      (YEAR             varchar(4)      not null, \
       CERTIF_KINDCD    varchar(3)      not null, \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CERTIF_KIND_YDAT add constraint PK_CERTIFKIND_YDAT primary key \
      (YEAR, CERTIF_KINDCD)


