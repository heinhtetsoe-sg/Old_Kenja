<?php

require_once('for_php7.php');

class knjp900Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjp900Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
    
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "INCOME_L_CD"          =>  VARS::post("INCOME_L_CD"),          //収入科目
                             "INCOME_L_M_CD"        =>  VARS::post("INCOME_L_M_CD"),        //収入項目
                             "REQUEST_NO"           =>  VARS::post("REQUEST_NO"),           //伝票番号
                             "SEARCH_YMONTH"        =>  VARS::post("SEARCH_YMONTH"),        //伺い対象月
                             "REQUEST_DATE"         =>  VARS::post("REQUEST_DATE"),         //伺い日
                             "INCOME_DATE"          =>  VARS::post("INCOME_DATE"),          //収入日
                             "INCOME_APPROVAL"      =>  VARS::post("INCOME_APPROVAL"),      //決裁済
                             "NOT_INCOME_APPROVAL"  =>  VARS::post("NOT_INCOME_APPROVAL"),  //未決裁
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
