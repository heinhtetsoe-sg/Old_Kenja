<?php

require_once('for_php7.php');

class knjp904Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjp904Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
    
        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "LEVY_L_CD"        =>  VARS::post("LEVY_L_CD"),        //預り金科目
                             "OUTGO_L_M_CD"     =>  VARS::post("OUTGO_L_M_CD"),     //支出項目
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "REQUEST_DATE"     =>  VARS::post("REQUEST_DATE"),     //伺い日
                             "OUTGO_DATE"       =>  VARS::post("OUTGO_DATE")        //支出日
                            );

        if (VARS::get("SEND_PRGID")) {
            $this->field["SCHOOL_KIND"] = VARS::get("SEND_SCHOOL_KIND");
            $this->field["LEVY_L_CD"]   = VARS::get("SEND_OUTGO_L_CD");
        }

        $this->cmd = VARS::request("cmd");

        $this->schoolCd = (sprintf("%012d", SCHOOLCD));

        $this->year     = CTRL_YEAR;
        $this->lastYear = (CTRL_YEAR - 1);

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
