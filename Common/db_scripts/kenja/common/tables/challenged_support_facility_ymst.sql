-- kanji=����
-- $Id: d5c15ccb9908f4d96b41135af4a89936ecc0f936 $

-- �ƥ��Ȼ��ֳ䤫��ֺ¤μ»������Ǽ���������Ϥǻ��ѡ�
-- ������: 2005/05/16 20:09:00 - JST
-- ������: tamura

-- ������ץȤλ�����ˡ: db2 +c -f <thisfile>
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���

drop   table CHALLENGED_SUPPORT_FACILITY_YMST

create table CHALLENGED_SUPPORT_FACILITY_YMST ( \
    YEAR                varchar(4) not null, \
    SPRT_FACILITY_CD    varchar(3) not null, \
    SPRT_FACILITY_NAME  varchar(30) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE CHALLENGED_SUPPORT_FACILITY_YMST ADD CONSTRAINT PK_CHAL_SUPFAC_YM PRIMARY KEY (YEAR, SPRT_FACILITY_CD)
