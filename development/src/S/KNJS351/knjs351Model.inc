<?php

require_once('for_php7.php');

class knjs351Model extends Model {
    var $field = array();
    var $cmd;

    function knjs351Model() {}

    function init() {
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"         => VARS::post("SEMESTER"),          //学期
                             "HR_CLASS_TYPE"    => VARS::post("HR_CLASS_TYPE"),     //クラス方式
                             "GAKUNEN_KONGOU"   => VARS::post("GAKUNEN_KONGOU"),    //学年混合チェックボックス
                             "GRADE_HR_CLASS"   => VARS::post("GRADE_HR_CLASS"),    //対象クラス
                             "FORM_SELECT"      => VARS::post("FORM_SELECT"),       //帳票選択
                             "DBNAME"           => VARS::post("DBNAME")             //DB変更に備えて
                            );
    }
}
?>
