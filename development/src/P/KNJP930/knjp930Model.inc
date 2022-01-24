<?php

require_once('for_php7.php');

class knjp930Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjp930Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
    
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEKOU_L_CD"       =>  VARS::post("SEKOU_L_CD"),       //施行科目
                             "SEKOU_L_M_CD"     =>  VARS::post("SEKOU_L_M_CD"),     //施行項目
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "REQUEST_DATE"     =>  VARS::post("REQUEST_DATE")      //伺い日
                            );

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
