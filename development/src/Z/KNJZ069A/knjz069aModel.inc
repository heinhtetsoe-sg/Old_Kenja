<?php

require_once('for_php7.php');

class knjz069aModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $name;
    var $field = array();
    var $control;

    function knjz069aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->name);
            unset($this->exp_grade);
        }

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),   //学期
                             "GRADE"        =>  VARS::post("GRADE"),      //学年
                             "UNIT_CD"      =>  VARS::post("UNIT_CD"),    //Unit区分
                             "UNIT_THEME"   =>  VARS::post("UNIT_THEME"), //教科の枠を超えたテーマ
                             "UNIT_IDEA"    =>  VARS::post("UNIT_IDEA"),  //中心概念
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->exp_grade    = VARS::get("GRADE");
            $db = Query::dbCheckOut();
            Query::dbCheckIn($db);
        }

        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->name);
            unset($this->exp_grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //出欠の様子備考
        $this->getPro["UNIT_THEME"]["moji"] = 30;
        $this->getPro["UNIT_THEME"]["gyou"] = 2;

        //特別活動・係
        $this->getPro["UNIT_IDEA"]["moji"] = 30;
        $this->getPro["UNIT_IDEA"]["gyou"] = 4;
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG301","学期");
            return false;
        }
        if ($this->field["GRADE"] == "") {
            $this->setWarning("MSG301","学年");
            return false;
        }
        if ($this->field["UNIT_CD"] == "") {
            $this->setWarning("MSG301","Unit区分");
            return false;
        }
        //文字数チェック
        if ($this->validate_row_cnt($this->field["UNIT_THEME"], ($this->getPro["UNIT_THEME"]["moji"] * 2)) > $this->getPro["UNIT_THEME"]["gyou"]) {
            $this->setWarning("MSG915","(教科の枠を超えたテーマは{$this->getPro["UNIT_THEME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["UNIT_IDEA"], ($this->getPro["UNIT_IDEA"]["moji"] * 2)) > $this->getPro["UNIT_IDEA"]["gyou"]) {
            $this->setWarning("MSG915","(中心概念は{$this->getPro["UNIT_IDEA"]["gyou"]}行までです)");
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $string1 = str_replace(array(" ", "　","\r\n", "\r", "\n"), "", $this->field["UNIT_THEME"]);
        $string2 = str_replace(array(" ", "　","\r\n", "\r", "\n"), "", $this->field["UNIT_IDEA"]);

        $this->field["UNIT_THEME"] = ($string1 == "") ? "" : $this->field["UNIT_THEME"];
        $this->field["UNIT_IDEA"] = ($string2 == "") ? "" : $this->field["UNIT_IDEA"];

        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjz069aQuery::getInsertQuery($this);
        return ;
    }
}
?>
