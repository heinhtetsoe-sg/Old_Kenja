<?php

require_once('for_php7.php');

class knjs341Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjs341Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("GRADE"            => VARS::post("GRADE"),             //対象クラス
                             "HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),    //クラス方式
                             "DBNAME"           => VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");
        
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
