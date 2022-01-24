<?php

require_once('for_php7.php');

// kanji=����
// $Id: knjb0056Model.inc,v 1.3 2012/07/27 07:30:02 maesiro Exp $
class knjb0056Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   // �R���g���[���}�X�^
    var $kouji;     // �Z���R�[�h

    function KNJB0056Model() {
        //�R���g���[���}�X�^�������擾
        common::GetControlMaster_Fnc($this->control);
    }

    /**
     * ������
     */
    function init() {
        //�v���O����ID
        $programid = VARS::get("PROGRAMID");
        if (isset($programid)){
            $this->programid = VARS::get("PROGRAMID");
        }

        //���ȃR�[�h
        $classcd = VARS::get("classcd");
        if (isset($classcd)){
            $this->classcd = VARS::get("classcd");
        }

        //�w�Z���
        $school_kind = VARS::get("school_kind");
        if (isset($school_kind)){
            $this->school_kind = VARS::get("school_kind");
        }

        //����ے��R�[�h
        $curriculum_cd = VARS::get("curriculum_cd");
        if (isset($curriculum_cd)){
            $this->curriculum_cd = VARS::get("curriculum_cd");
        }

        //�ȖڃR�[�h
        $subclasscd = VARS::get("subclasscd");
        if (isset($subclasscd)){
            $this->subclasscd = VARS::get("subclasscd");
        }

        //�N�x
        $year = VARS::get("year");
        if (isset($year)){
            $this->year = VARS::get("year");
        }
        //�w��
        $semester = VARS::get("semester");
        if (isset($semester)){
            $this->semester = VARS::get("semester");
        }

        $this->cmd   = VARS::request("cmd");

        //�����`�F�b�N
        $this->auth = common::SecurityCheck(STAFFCD, $this->programid);

        $this->getPropertiesAll();
    }

    /**
     * �ϐ����N���A���܂�
     */
    function clean() {}
}
?>
