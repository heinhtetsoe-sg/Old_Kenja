<?php

require_once('for_php7.php');

class knjd129iModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjd129iModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GROUP_CD"         =>  VARS::post("GROUP_CD"),         //コース
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数

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

        if ($this->field["GRADE"] == "") {
            $this->setWarning("MSG304", "( 学年 )");
            return false;
        }

        if ($this->field["GROUP_CD"] == "") {
            $this->setWarning("MSG304", "( コース )");
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
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            $score_pass_flg = "SCORE_PASS_FLG"."-".$i;
            $score_pass = "SCORE_PASS"."-".$i;
            $this->fields["SCORE_PASS_FLG"][] = VARS::post($score_pass_flg);
            $this->fields["SCORE_PASS"][] = VARS::post($score_pass);
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
        }

        if ($this->validateInput()) {
            knjd129iQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
