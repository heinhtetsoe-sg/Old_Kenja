-- kanji=����
-- $Id: rec_credit_admits.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

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
