-- kanji=����
-- $Id: 331057d79558a8ef2bbadfb115ab578f2a71f689 $

-- �ƥ��Ȼ��ֳ䤫��ֺ¤μ»������Ǽ���������Ϥǻ��ѡ�
-- ������: 2005/05/16 20:09:00 - JST
-- ������: tamura

-- ������ץȤλ�����ˡ: db2 +c -f <thisfile>
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���

drop   table SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT

create table SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT ( \
    YEAR                varchar(4) not null, \
    SCHREGNO            varchar(8) not null, \
    SPRT_FACILITY_CD    varchar(3) not null, \
    SEQ                 varchar(2) not null, \
    REMARK              varchar(610), \
    STATUS              varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT ADD CONSTRAINT PK_SCHCHAL_SPL_FAC PRIMARY KEY (YEAR, SCHREGNO, SPRT_FACILITY_CD, SEQ)
