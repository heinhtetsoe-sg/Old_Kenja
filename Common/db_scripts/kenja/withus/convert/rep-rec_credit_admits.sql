-- kanji=漢字
-- $Id: rep-rec_credit_admits.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table REC_CREDIT_ADMITS_OLD

create table REC_CREDIT_ADMITS_OLD like REC_CREDIT_ADMITS

insert into REC_CREDIT_ADMITS_OLD select * from REC_CREDIT_ADMITS

drop table REC_CREDIT_ADMITS

create table REC_CREDIT_ADMITS \
(  \
    YEAR            varchar(4) not null, \
    CLASSCD         varchar(2) not null, \
    CURRICULUM_CD   varchar(1) not null, \
    SUBCLASSCD      varchar(6) not null, \
    SCHREGNO        varchar(8) not null, \
    USUAL_SCORE     smallint, \
    TOTAL_SCORE     decimal (4,1), \
    AUTO_GRAD_VALUE smallint, \
    GRAD_VALUE      smallint, \
    GET_CREDIT      smallint, \
    SCHOOLING_ASSESS smallint, \
    REPORT_AVG      decimal (4,1), \
    TEST_SCORE      smallint, \
    COMBINED_FLG    varchar(1), \
    ADMITS_FLG      varchar(1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REC_CREDIT_ADMITS  \
add constraint PK_REC_CREDIT_ADMI \
primary key  \
(YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)

insert into REC_CREDIT_ADMITS \
select \
    YEAR, \
    CLASSCD, \
    CURRICULUM_CD, \
    SUBCLASSCD, \
    SCHREGNO, \
    USUAL_SCORE, \
    TOTAL_SCORE, \
    GRAD_VALUE, \
    GRAD_VALUE, \
    GET_CREDIT, \
    SCHOOLING_ASSESS, \
    REPORT_AVG, \
    TEST_SCORE, \
    COMBINED_FLG, \
    '1', \
    REGISTERCD, \
    UPDATED \
FROM \
    REC_CREDIT_ADMITS_OLD
