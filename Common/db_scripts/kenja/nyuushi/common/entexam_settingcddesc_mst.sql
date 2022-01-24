-- kanji=����
-- $Id: 197dc757172fdccbc6e315698e3cd564d47f8ea2 $

-- �����ݒ�}�X�^(�����p���̃}�X�^)
-- ����:���̃t�@�C���� EUC/LF�̂� �łȂ���΂Ȃ�Ȃ��B
-- �K�p���@:
--    1.�f�[�^�x�[�X�ڑ�
--    2.db2 +c -f <���̃t�@�C��>
--    3.�R�~�b�g����Ȃ�Adb2 +c commit�B��蒼���Ȃ�Adb2 +c rollback

drop table ENTEXAM_SETTINGCDDESC_MST

create table ENTEXAM_SETTINGCDDESC_MST ( \
    SETTING_CD      varchar(4) not null, \
    CDMEMO          varchar(120), \
    MODIFY_FLG      varchar(1), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SETTINGCDDESC_MST add constraint PK_ENTEXAM_SETTINGCDDESC_MST primary key (SETTING_CD)
