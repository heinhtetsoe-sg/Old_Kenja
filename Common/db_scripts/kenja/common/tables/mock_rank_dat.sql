-- kanji=����
-- $Id: 6911c3f85599196f91cb8ec0c074900a5023f47c $
-- �ƥ��ȹ��ܥޥ������ץե饰

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table MOCK_RANK_DAT

create table MOCK_RANK_DAT ( \
    YEAR             varchar(4) not null, \
    MOCKCD           varchar(9) not null, \
    SCHREGNO         varchar(8) not null, \
    MOCK_SUBCLASS_CD varchar(6) not null, \
    MOCKDIV          varchar(1) not null, \
    SCORE            smallint, \
    AVG              decimal (8,5), \
    GRADE_RANK       smallint, \
    GRADE_DEVIATION  decimal (4,1), \
    CLASS_RANK       smallint, \
    CLASS_DEVIATION  decimal (4,1), \
    COURSE_RANK      smallint, \
    COURSE_DEVIATION decimal (4,1), \
    REGISTERCD       varchar(8), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_RANK_DAT add constraint PK_MOCK_RANK_DAT \
      primary key (YEAR, MOCKCD, SCHREGNO, MOCK_SUBCLASS_CD, MOCKDIV)
