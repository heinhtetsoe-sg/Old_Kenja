<?php

require_once('for_php7.php');

class knjg085Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjg085Model() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->fields = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック

        if ($this->field["GRADE_HR_CLASS"] == "") {
            $this->setWarning("MSG304", "( 年組 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $ydat_name1  = "YDAT_REMARK1"."_".$i;
            $ydat_name10 = "YDAT_REMARK10"."_".$i;
            $this->fields["YDAT_REMARK1"][] = VARS::post($ydat_name1);
            $this->fields["YDAT_REMARK10"][] = str_replace("/","-",VARS::post($ydat_name10));
        }

        if ($this->validateInput()) {
            knjg085Query::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
