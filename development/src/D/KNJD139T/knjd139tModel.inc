<?php

require_once('for_php7.php');

class knjd139tModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;

    function knjd139tModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_grade);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),         //学期
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"), //出欠の様子備考
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"), //特別活動・係
                             "REMARK1"              =>  VARS::post("REMARK1"),          //フィールドワーク等
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->exp_grade    = VARS::get("GRADE");
            $db = Query::dbCheckOut();
            Query::dbCheckIn($db);
        }

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //出欠の様子備考
        $this->getPro["ATTENDREC_REMARK"]["moji"] = 20;
        $this->getPro["ATTENDREC_REMARK"]["gyou"] = 5;

        //特別活動・係
        $this->getPro["SPECIALACTREMARK"]["moji"] = 12;
        $this->getPro["SPECIALACTREMARK"]["gyou"] = 1;

        //フィールドワーク等
        $this->getPro["REMARK1"]["moji"] = 12;
        $this->getPro["REMARK1"]["gyou"] = 1;
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出欠の様子備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ($this->getPro["SPECIALACTREMARK"]["moji"] * 2)) > $this->getPro["SPECIALACTREMARK"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動・係は{$this->getPro["SPECIALACTREMARK"]["moji"]}文字までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(フィールドワーク等は{$this->getPro["REMARK1"]["moji"]}文字までです)");
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $string1 = str_replace(array(" ", "　","\r\n", "\r", "\n"), "", $this->field["ATTENDREC_REMARK"]);
        $string2 = str_replace(array(" ", "　"), "", $this->field["SPECIALACTREMARK"]);
        $string3 = str_replace(array(" ", "　"), "", $this->field["REMARK1"]);

        $this->field["ATTENDREC_REMARK"] = ($string1 == "") ? "" : $this->field["ATTENDREC_REMARK"];
        $this->field["SPECIALACTREMARK"] = ($string2 == "") ? "" : $this->field["SPECIALACTREMARK"];
        $this->field["REMARK1"]          = ($string3 == "") ? "" : $this->field["REMARK1"];

        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjd139tQuery::getInsertQuery($this);
        return ;
    }
}
?>
