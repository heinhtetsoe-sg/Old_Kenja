-- kanji=����
-- $Id: 7284491770f55d8f0bffa80ae5299ed5438a93f0 $

-- �ƥ��Ȼ��ֳ䤫��ֺ¤μ»������Ǽ���������Ϥǻ��ѡ�
-- ������: 2005/05/16 20:09:00 - JST
-- ������: tamura

-- ������ץȤλ�����ˡ: db2 +c -f <thisfile>
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���

drop   table CHALLENGED_SUPPORT_BASE_INFO_YMST

create table CHALLENGED_SUPPORT_BASE_INFO_YMST ( \
    YEAR        varchar(4) not null, \
    SPRT_SEQ    varchar(2) not null, \
    BASE_TITLE  varchar(150) not null, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE CHALLENGED_SUPPORT_BASE_INFO_YMST ADD CONSTRAINT PK_CHAL_SUPBASE_YM PRIMARY KEY (YEAR, SPRT_SEQ)
