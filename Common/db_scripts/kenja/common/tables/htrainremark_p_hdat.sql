-- kanji=漢字
-- $Id: 3ca16e32bf8fd2e9dd9d87aedc4b06b04fe6a65f $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table HTRAINREMARK_P_HDAT

create table HTRAINREMARK_P_HDAT \
    (SCHREGNO             varchar(8) not null, \
     TOTALSTUDYACT        varchar(534), \
     TOTALSTUDYVAL        varchar(802), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HTRAINREMARK_P_HDAT \
add constraint PK_HTRAINREMARKP_H \
primary key \
(SCHREGNO)
