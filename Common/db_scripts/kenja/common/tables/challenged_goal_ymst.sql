-- kanji=����
-- $Id: e4d45e0750f2b09e03de2661f75ec9d3562605bb $

-- �ƥ��Ȼ��ֳ䤫��ֺ¤μ»������Ǽ���������Ϥǻ��ѡ�
-- ������: 2005/05/16 20:09:00 - JST
-- ������: tamura

-- ������ץȤλ�����ˡ: db2 +c -f <thisfile>
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���

drop   table CHALLENGED_GOAL_YMST

create table CHALLENGED_GOAL_YMST ( \
    YEAR        varchar(4) not null, \
    SPRT_SEQ    varchar(2) not null, \
    GOAL_TITLE  varchar(150) not null, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE CHALLENGED_GOAL_YMST ADD CONSTRAINT PK_CHAL_GOAL_YM PRIMARY KEY (YEAR, SPRT_SEQ)
