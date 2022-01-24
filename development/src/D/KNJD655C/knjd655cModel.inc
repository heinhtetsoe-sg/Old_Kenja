<?php

require_once('for_php7.php');

class knjd655cModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $moji;

    function knjd655cModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "RIYUU"            =>  VARS::post("RIYUU"),            //理由
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->getPropertiesAll(); //プロパティファイルの読込み
        $this->cmd = VARS::request("cmd");
        if (!in_array($this->field["RIYUU"], array('5','6'))) {
            $this->field["RIYUU"] = '5';
        }

        if ($this->field["RIYUU"] == '5') {
            $this->moji = 15;
        } else {
            $this->moji = 40;
        }
    }

    //入力項目チェック
    function validateInput() {
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
        $byte = $this->moji * 3;
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if (strlen($this->fields["REMARK"][$i]) > $byte) {
                $this->setWarning("MSG915","( 理由は{$byte}バイトまでです )");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $remark_name = "REMARK-".$i;
            $check_name  = "CHECK-".$i;
            $this->fields["REMARK"][] = VARS::post($remark_name);
            $this->fields["CHECK"][]  = VARS::post($check_name);
        }

        if ($this->validateInput()) {
            knjd655cQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
