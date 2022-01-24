-- kanji=����
-- $Id: b34f195a640f1c96096f283f564cc816edfa8d08 $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ����:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table MOCK_AVERAGE_DAT

create table MOCK_AVERAGE_DAT ( \
    YEAR                varchar(4) not null, \
    MOCKCD              varchar(9) not null, \
    MOCK_SUBCLASS_CD    varchar(6) not null, \
    AVG_DIV             varchar(1) not null, \
    GRADE               varchar(2) not null, \
    HR_CLASS            varchar(3) not null, \
    COURSECD            varchar(1) not null, \
    MAJORCD             varchar(3) not null, \
    COURSECODE          varchar(4) not null, \
    SCORE               integer, \
    SCORE_KANSAN        integer, \
    HIGHSCORE           integer, \
    HIGHSCORE_KANSAN    integer, \
    LOWSCORE            integer, \
    LOWSCORE_KANSAN     integer, \
    COUNT               smallint, \
    AVG                 decimal (9,5), \
    AVG_KANSAN          decimal (9,5), \
    STDDEV              decimal (5,1), \
    STDDEV_KANSAN       decimal (5,1), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_AVERAGE_DAT add constraint PK_MOCK_AVERAGE_D \
      primary key (YEAR, MOCKCD, MOCK_SUBCLASS_CD, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)