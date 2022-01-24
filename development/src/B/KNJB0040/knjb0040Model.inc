<?php

require_once('for_php7.php');

// kanji=����
// $Id: knjb0040Model.inc,v 1.3.62.1 2012/08/01 00:26:33 maesiro Exp $
class knjb0040Model extends Model {
	var $field = array();
	var $cmd;
	var $control;   // �R���g���[���}�X�^
	var $staffcd;   // �E���R�[�h
	var $kouji;     // �Z���R�[�h

    /**
     * �R���X�g���N�^
     */
    function knjb0040Model()
    {
        //�R���g���[���}�X�^�������擾
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;	// �E���R�[�h
    }

    /**
     * ������
     */
    function init()
    {
        //�v���O����ID
        $programid = VARS::get("PROGRAMID");
        if (isset($programid)){
            $this->programid = VARS::get("PROGRAMID");
        }

        //�������ύX���ꂽ�ꍇ
        if ($this->staffcd != STAFFCD){
            $this->staffcd      = STAFFCD;                  //�E���R�[�h
        }
        $this->cmd   = VARS::request("cmd");

        //�����`�F�b�N
        $this->auth = common::SecurityCheck($this->staffcd, $this->programid);

        $this->getPropertiesAll();
    }

    /**
     * �ϐ����N���A���܂�
     */
    function clean()
    {
    }
}
?>
