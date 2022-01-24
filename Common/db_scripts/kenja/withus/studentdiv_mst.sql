-- kanji=漢字
-- $Id: studentdiv_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table STUDENTDIV_MST

create table STUDENTDIV_MST \
(  \
        COURSE_DIV        varchar(1)  not null, \
        STUDENT_DIV       varchar(2)  not null, \
        NAME              varchar(30) not null, \
        COMMUTING_DIV     varchar(1), \
        REGISTERCD        varchar(8), \
        UPDATED           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table STUDENTDIV_MST \
add constraint PK_STUDENTDIV_MST \
primary key  \
(COURSE_DIV, STUDENT_DIV)
