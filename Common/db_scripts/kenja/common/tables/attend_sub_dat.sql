-- kanji=漢字
-- $Id: 2ab120d4f2e062092ceba9a624d82c5d8e9e3560 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEND_SUB_DAT

create table ATTEND_SUB_DAT \
        (SCHREGNO           varchar(8)      not null, \
         EXECUTEDATE        date            not null, \
         DI_CD              varchar(2)      not null, \
         SUBL_CD            varchar(3)      not null, \
         SUBM_CD            varchar(3), \
         REGISTERCD         varchar(8), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_SUB_DAT add constraint pk_ATTEND_SUB_DAT primary key \
        (SCHREGNO, EXECUTEDATE)
        