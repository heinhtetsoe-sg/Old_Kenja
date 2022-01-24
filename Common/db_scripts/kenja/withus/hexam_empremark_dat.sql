-- kanji=漢字
-- $Id: hexam_empremark_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table HEXAM_EMPREMARK_DAT

create table HEXAM_EMPREMARK_DAT \
    (YEAR                   varchar(4) not null, \
     SCHREGNO               varchar(8) not null, \
     ANNUAL                 varchar(2), \
     JOBHUNT_REC            varchar(494), \
     JOBHUNT_ABSENCE        varchar(126), \
     REGISTERCD             varchar(8), \
     UPDATED                timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HEXAM_EMPREMARK_DAT add constraint PK_HEXAM_EMP_DAT primary key (YEAR, SCHREGNO)


