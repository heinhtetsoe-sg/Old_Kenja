<?php

require_once('for_php7.php');

class knjd651Model extends Model {

    var $field = array();
    var $data  = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjd651Model()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        
        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        //必須入力チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }
        if ($this->field["TESTKINDCD"] == "") {
            $this->setWarning("MSG304", "( テスト種別 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if (strlen($this->fields["REMARK"][$i]) > 90) {
                $this->setWarning("MSG915","( 備考は90バイトまでです )");
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
            $term_name = "REMARK"."-".$i;
            $this->fields["REMARK"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            knjd651Query::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
