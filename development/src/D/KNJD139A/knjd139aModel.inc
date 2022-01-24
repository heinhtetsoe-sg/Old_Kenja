<?php

require_once('for_php7.php');

class knjd139aModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjd139aModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "SEMESTER2"        =>  VARS::post("SEMESTER2"),        //学期(観点以外)
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

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

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        //桁数、バイトチェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["REMARK1"][$i], (20 * 2)) > 5) {
                $this->setWarning("MSG915","(コメントは5行までです)");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $term_name = "REMARK1"."-".$i;
            $this->fields["REMARK1"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            knjd139aQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
