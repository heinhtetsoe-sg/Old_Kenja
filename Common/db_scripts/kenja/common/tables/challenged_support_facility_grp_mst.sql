-- kanji=����
-- $Id: 9c956353a57388c6434efe71597cc59f8953fda3 $

-- �ƥ��Ȼ��ֳ䤫��ֺ¤μ»������Ǽ���������Ϥǻ��ѡ�
-- ������: 2005/05/16 20:09:00 - JST
-- ������: tamura

-- ������ץȤλ�����ˡ: db2 +c -f <thisfile>
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���

drop   table CHALLENGED_SUPPORT_FACILITY_GRP_MST

create table CHALLENGED_SUPPORT_FACILITY_GRP_MST ( \
    YEAR                varchar(4) not null, \
    SPRT_FACILITY_GRP   varchar(3) not null, \
    SPRT_FACIL_GRP_NAME varchar(30) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE CHALLENGED_SUPPORT_FACILITY_GRP_MST ADD CONSTRAINT PK_CHALSPRT_FACGM PRIMARY KEY (YEAR, SPRT_FACILITY_GRP)
