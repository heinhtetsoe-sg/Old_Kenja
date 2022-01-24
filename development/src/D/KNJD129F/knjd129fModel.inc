<?php

require_once('for_php7.php');

class knjd129fModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $nyuryoku;
    var $type_div;

    function knjd129fModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
            $this->callPrgid = "";
            $this->auth = "";
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        $this->nyuryoku = VARS::post("NYURYOKU");
        $this->type_div = VARS::post("TYPE_DIV");
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数

        $this->cmd     = VARS::request("cmd");
        if (VARS::get("CALL_PRGID")) {
            $this->callPrgid = VARS::get("CALL_PRGID");
            $this->auth = VARS::get("SEND_AUTH");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }

        if ($this->field["SUBCLASSCD"] == "") {
            $this->setWarning("MSG304", "( 科目 )");
            return false;
        }

        if ($this->field["COUNTER"] == 0) {
            $this->setWarning("MSG303");
            return false;
        }
/***
        //数値チェック
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            if ($this->fields["VALUE"][$i]) {
                if (!is_numeric($this->fields["VALUE"][$i])) {
                    $this->setWarning("MSG915","(評価は数値を入力して下さい。)");
                    return false;
                }
            }
        }
***/
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            $term_name = "VALUE"."-".$i;
            $term_name_val = "VALUE"."_FORM_ID"."-".$i;
            $this->fields["VALUE"][] = VARS::post($term_name) ? VARS::post($term_name_val) : "";
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
        }

        if ($this->validateInput()) {
            knjd129fQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
