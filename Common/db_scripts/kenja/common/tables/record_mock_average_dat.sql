-- kanji=����
-- $Id: 0200b7dd091411a4c5c67040be8a44dc79038a53 $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table RECORD_MOCK_AVERAGE_DAT

create table RECORD_MOCK_AVERAGE_DAT ( \
    YEAR            varchar(4) not null, \
    SUBCLASSCD      varchar(6) not null, \
    AVG_DIV         varchar(1) not null, \
    GRADE           varchar(2) not null, \
    HR_CLASS        varchar(3) not null, \
    COURSECD        varchar(1) not null, \
    MAJORCD         varchar(3) not null, \
    COURSECODE      varchar(4) not null, \
    SCORE           integer, \
    HIGHSCORE       integer, \
    LOWSCORE        integer, \
    COUNT           smallint, \
    AVG             decimal (9,5), \
    STDDEV          decimal (5,1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECORD_MOCK_AVERAGE_DAT add constraint pk_rec_mock_avg_d \
      primary key (YEAR, SUBCLASSCD, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)
