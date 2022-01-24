-- kanji=漢字
-- $Id: fb0bc03a2e6717fd265b444e0f74f367ccbc8d74 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table GRD_HTRAINREMARK_HDAT

create table HTRAINREMARK_HDAT ( \
     SCHREGNO             varchar(8) not null, \
     TOTALSTUDYACT        varchar(534), \
     TOTALSTUDYVAL        varchar(802), \
     TOTALSTUDYACT2       varchar(534), \
     TOTALSTUDYVAL2       varchar(802), \
     CREDITREMARK         varchar(802), \
     REGISTERCD           varchar(10), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

ALTER TABLE GRD_HTRAINREMARK_HDAT ADD CONSTRAINT PK_HTRAINR_D PRIMARY KEY (SCHREGNO)