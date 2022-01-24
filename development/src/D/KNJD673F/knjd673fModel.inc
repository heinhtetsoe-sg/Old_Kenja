<?php

require_once('for_php7.php');

class knjd673fModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjd673fModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "INTERVIEW_DIV"    =>  VARS::post("INTERVIEW_DIV"),    //面談区分
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

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
        if ($this->field["INTERVIEW_DIV"] == "") {
            $this->setWarning("MSG304", "( 面談区分 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        //桁数、バイトチェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["REMARK"][$i], (45 * 2)) > 2) {
                $this->setWarning("MSG915","(コメントは2行までです)");
                return false;
            }
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $name = "REMARK"."-".$i;
            $this->fields["REMARK"][] = VARS::post($name);
        }

        if ($this->validateInput()) {
            knjd673fQuery::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
