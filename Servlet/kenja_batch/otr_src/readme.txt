
*** OTR(�J�[�h���[�_�[)����̏o�ȃf�[�^���̓v���O���� ***



��OtrRead.tar�Ɋ܂܂��t�@�C���Ƃ��̓��e�͈ȉ��̒ʂ�ł��B
�EOtrRead.x.x.x.jar       �E�E�E���sjar�v���O�����t�@�C�� (x.x.x:�o�[�W����)
�Elog4j.properties        �E�E�E���O�o�̓t�H�[�}�b�g�t�@�C��
�Eotr_read.sh.DB2v7       �E�E�E���s�X�N���v�g(DB2v7�p)
�Eotr_read.sh.DB2v8       �E�E�E���s�X�N���v�g(DB2v8�p)
�EOtrRead.properties      �E�E�E�v���O�����̃v���p�e�B�[�t�@�C��
�EPeriodTimeTable.properties �E�E�E�Z���̃^�C���e�[�u��
�Eotr_TAG_HISTORY.txt     �E�E�E�X�V����
�Eotr_html_spec/OtrRead.html�E�E�EHTML�d�l��
�Ereadme.txt

�v���O�������s�̃C���[�W�Ƃ��Ă�
�C���X�g�[���ō쐬����otr_read.sh ��DB���̈�����ǉ����Ď��s���܂��ƁA
�N���������t�A�N���������Ԃ܂ł̍Z���́A�o�������{�̎��Ƃ�
�o���f�[�^���AOTR���o�͂����t�@�C������ǂݎ��A
�w���DB�̉��o���f�[�^�e�[�u��(ATTEND2_DAT)�ɏ������݂܂��B

�Z���Ǝ��Ԃ̐ݒ�� PeriodTimeTable.properties �ōs���܂��B
OTR���o�͂����t�@�C���̏ꏊ�� OtrRead.properties �Ŏw�肵�܂��B
�v���O�����̃��O�̐ݒ��log4j.properties�Ŏw�肵�܂�
(���O�̏ꏊ�̓f�t�H���g��/var/tmp/otr_read.xx.log�Ƃ��Ă��܂�)�B


���C���X�g�[���̎�������ȉ��Ɏ����܂��B

(1) OtrRead.properties ��ҏW���Ă��������B

  kintaiFilePath �� OTR���o�͂���Αӂ̃t�@�C���̃p�X���w�肵�܂��B
    (��: kintaiFilePath = /usr/local/development/DAKINTAI.txt )


(2) PeriodTimeTable.properties ��ҏW���܂��B

  �Z���̊J�n���ԁA�I�����Ԃ��w�肵�܂��B���̃t�H�[�}�b�g��
  1����1�s�̋L�q�ł��肢���܂��B
    �t�H�[�}�b�g : [����(���̃}�X�^NAMECD1='B001'��NAMECD2�̕�����)] = [�J�n����],[�I������]
    (��: 1 = 08:45,09:35)


(3) otr_read.sh ���쐬���܂��B

  �g�p����DB2�̃o�[�W�����ɂ����
  �ȉ��̃t�@�C���R�s�[�R�}���h��I�����A���s���܂��B
  (a)DB2�̃o�[�W������7
    cp otr_read.sh.DB2v7 otr_read.sh
  (b)DB2�̃o�[�W������8
    cp otr_read.sh.DB2v8 otr_read.sh


(4) otr_read.sh�Ɏ��s���[�h��ǉ����܂�

  ���̃R�}���h�����s���܂��B
    chmod +x otr_read.sh


(5) �����NOtrRead.jar���쐬���܂��B

  ���̃R�}���h�����s���܂��B(x.x.x:�o�[�W����)
    ln -s OtrRead.x.x.x.jar OtrRead.jar 


(6) cron �Ŏw�莞�Ԃ� otr_read.sh �����s�����悤�ɃZ�b�g���܂��B

	(��: �ȉ���root������/etc/crontab�ɒǉ����܂��B
	(    ��DBNAME�͎��ۂɂ�DB�̖��̂ł�
	35  9 * * 0-5 root /tmp/otr_read.sh DBNAME # ���j~���j��09:35��root��/tmp/otr_read.sh DBNAME �����s
	25 10 * * 0-5 root /tmp/otr_read.sh DBNAME #            10:25
	35 11 * * 0-5 root /tmp/otr_read.sh DBNAME #            11:35
	...


(7) cron�Ŏw�莞�Ԃ� OTR���o�͂����Αӂ̃t�@�C�����폜����悤�ɃZ�b�g���܂�
    (��: �ȉ���root������/etc/crontab�ɒǉ����܂��B
	(    ��DAKINTAI.txt�͎��ۂɂ�OTR���o�͂����Αӂ̃t�@�C���̖��̂ł�
	0 0 * * * root rm DAKINTAI.txt # �����[��0����DAKINTAI.txt���폜����
