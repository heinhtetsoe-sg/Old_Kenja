-- kanji=����
-- $Id: 3b8c3e767fc73ef51114c951cf9ac6b7132d97aa $

-- �ƥ��Ȼ��ֳ䤫��ֺ¤μ»������Ǽ���������Ϥǻ��ѡ�
-- ������: 2005/10/12
-- ������: m-yama

-- ������ץȤλ�����ˡ: db2 +c -f <thisfile>
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���

drop   table SCH_CHR_TEST_OLD

create table SCH_CHR_TEST_OLD like SCH_CHR_TEST

insert into SCH_CHR_TEST_OLD select * from SCH_CHR_TEST

drop   table SCH_CHR_TEST

create table SCH_CHR_TEST ( \
    EXECUTEDATE date not null, \
    PERIODCD    varchar(1) not null, \
    CHAIRCD     varchar(7) not null, \
    TESTKINDCD  varchar(2), \
    TESTITEMCD  varchar(2), \
    EXECUTED    varchar(1), \
    YEAR        varchar(4), \
    SEMESTER    varchar(1), \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp, \
    primary key ( EXECUTEDATE, PERIODCD, CHAIRCD ) \
) in usr1dms index in idx1dms

insert into SCH_CHR_TEST select \
    EXECUTEDATE, \
    PERIODCD, \
    CHAIRCD, \
    TESTKINDCD, \
    TESTITEMCD, \
    '0', \
    YEAR, \
    SEMESTER, \
    REGISTERCD, \
    UPDATED \
from SCH_CHR_TEST_OLD
