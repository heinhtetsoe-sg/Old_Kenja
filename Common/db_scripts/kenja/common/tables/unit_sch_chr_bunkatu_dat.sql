-- kanji=����
-- $Id: ebce81b85c554361030ba411a758dbd693c4a2bf $

-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback
--
drop table UNIT_SCH_CHR_BUNKATU_DAT

create table UNIT_SCH_CHR_BUNKATU_DAT \
      (EXECUTEDATE        date         not null, \
       PERIODCD           varchar(1)   not null, \
       MAIN_CHAIRCD       varchar(7)   not null, \
       SEQ                smallint     not null, \
       CHAIRCD            varchar(7)   not null, \
       MINUTE             smallint     not null, \
       YEAR               varchar(4), \
       SEMESTER           varchar(1), \
       REGISTERCD         varchar(8), \
       UPDATED            timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table UNIT_SCH_CHR_BUNKATU_DAT add constraint PK_UNIT_SCH_CR_DAT primary key \
      (EXECUTEDATE, PERIODCD, MAIN_CHAIRCD, SEQ)
