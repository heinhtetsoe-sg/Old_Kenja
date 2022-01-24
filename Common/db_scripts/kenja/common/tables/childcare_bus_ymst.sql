-- kanji=漢字
-- $Id: 5494b7bb8d14ac7ec02f2595cf4d6e942555a1c5 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table CHILDCARE_BUS_YMST

create table CHILDCARE_BUS_YMST \
    (YEAR           varchar(4) not null, \
     COURSE_CD      varchar(2) not null, \
     SCHEDULE_CD    varchar(1) not null, \
     BUS_NAME       varchar(30), \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CHILDCARE_BUS_YMST add constraint PK_CHILD_BUSYMST primary key (YEAR, COURSE_CD)


