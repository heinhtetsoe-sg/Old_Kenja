-- kanji=漢字
-- $Id: 9782eff4b2442e168ddf50346713c41fb015cc31 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table DENGON

create table DENGON \
    (MESSAGENO    INTEGER not null, \
     SCHREGNO     varchar(8), \
     STAFFCD      varchar(8), \
     TERMDATE     DATE, \
     SENDDATE     DATE, \
     READDATE     DATE, \
     MESSAGE1     VARCHAR(90), \
     MESSAGE2     VARCHAR(90), \
     REGISTERCD   varchar(8), \
     UPDATED      timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table DENGON add constraint pk_dengon primary key (MESSAGENO)


