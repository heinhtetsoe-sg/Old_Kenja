-- kanji=漢字
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--


drop table SCHREG_IQ_DAT \

create table SCHREG_IQ_DAT \
      (YEAR              varchar(4) not null, \
       SCHREGNO          varchar(8) not null, \
       IQ                smallint, \
       REGISTERCD        varchar(10), \
       UPDATED           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_IQ_DAT add constraint PK_SCHREG_IQ_DAT primary key (YEAR, SCHREGNO)
