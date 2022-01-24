<?php

require_once('for_php7.php');

class knjh187Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjh187Model() {}

    function init() {

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("COURSECODE"       =>  VARS::post("COURSECODE"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $rosen_2        = "ROSEN_2-".$i;
            $josya_2        = "JOSYA_2-".$i;
            $gesya_2        = "GESYA_2-".$i;
            $this->fields["ROSEN_2"][]  = VARS::post($rosen_2);
            $this->fields["JOSYA_2"][]  = VARS::post($josya_2);
            $this->fields["GESYA_2"][]  = VARS::post($gesya_2);
        }

    }

    //入力項目チェック
    function validateInput() {
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["JOSYA_2"][$i], (15 * 2)) > 1) {
                $this->setWarning("MSG915","( 乗車名は全角15文字までです )");
                return false;
            }
            if ($this->validate_row_cnt($this->fields["GESYA_2"][$i], (15 * 2)) > 1) {
                $this->setWarning("MSG915","( 降車名は全角15文字までです )");
                return false;
            }
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjh187Query::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
