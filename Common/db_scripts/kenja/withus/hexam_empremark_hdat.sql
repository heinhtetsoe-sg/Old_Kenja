-- kanji=漢字
-- $Id: hexam_empremark_hdat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table HEXAM_EMPREMARK_HDAT

create table HEXAM_EMPREMARK_HDAT \
    (SCHREGNO               varchar(8) not null, \
     JOBHUNT_REC            varchar(494), \
     JOBHUNT_RECOMMEND      varchar(1278), \
     JOBHUNT_ABSENCE        varchar(126), \
     JOBHUNT_HEALTHREMARK   varchar(130), \
     REGISTERCD             varchar(8), \
     UPDATED                timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HEXAM_EMPREMARK_HDAT add constraint PK_HEXAM_EMP_HDAT primary key (SCHREGNO)


