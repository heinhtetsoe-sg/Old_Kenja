-- kanji=漢字
-- $Id: 893f28158296539f93e77ac12d838dbd8918bcfe $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table CERTIF_SCHOOL_DAT

create table CERTIF_SCHOOL_DAT \
      (YEAR             varchar(4)      not null, \
       CERTIF_KINDCD    varchar(3)      not null, \
       KINDNAME         varchar(24) , \
       CERTIF_NO		varchar(1)  , \
       SYOSYO_NAME	    varchar(30) , \
       SCHOOL_NAME	    varchar(90) , \
       JOB_NAME		    varchar(135), \
       PRINCIPAL_NAME	varchar(90) , \
       REMARK1		    varchar(150), \
       REMARK2		    varchar(150), \
       REMARK3		    varchar(150), \
       REMARK4		    varchar(150), \
       REMARK5		    varchar(150), \
       REMARK6		    varchar(150), \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CERTIF_SCHOOL_DAT add constraint PK_CERTSCL_DAT primary key \
      (YEAR,CERTIF_KINDCD)


