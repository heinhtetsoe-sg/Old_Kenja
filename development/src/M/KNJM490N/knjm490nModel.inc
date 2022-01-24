<?php

require_once('for_php7.php');

class knjm490nModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;
    var $isTuusin;

    function knjm490nModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    function init() {
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),             //年度
                             "NAME_NASI"         =>  VARS::post("NAME_NASI"),        //名前無し
                             "SEMESTER"          =>  VARS::post("SEMESTER"),         //学期
                             "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata  = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
