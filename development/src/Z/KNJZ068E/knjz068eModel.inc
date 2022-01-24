<?php

require_once('for_php7.php');

class knjz068eModel extends Model {
    var $ibprg_course;
    var $grade;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $field;

    function knjz068eModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->ibprg_course = VARS::post("IBPRG_COURSE");
        $this->grade = VARS::post("GRADE");
        $this->cmd      = VARS::request("cmd");
        $this->field = array(
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        if ($this->cmd == '') {
            unset($this->ibprg_course);
            unset($this->grade);
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
