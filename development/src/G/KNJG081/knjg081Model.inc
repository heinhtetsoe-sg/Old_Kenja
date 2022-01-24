<?php

require_once('for_php7.php');

class knjg081Model extends Model {

    var $field = array();
    var $data  = array();
    var $cmd;

    function knjg081Model()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        //必須入力チェック
        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if (strlen($this->fields["PRINT_CNT"][$i]) > 2) {
                $this->setWarning("MSG915","( 発行枚数は2バイトまでです )");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $term_name = "PRINT_CNT"."-".$i;
            $this->fields["PRINT_CNT"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            knjg081Query::update($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
