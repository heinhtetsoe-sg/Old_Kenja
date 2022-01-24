<?php

require_once('for_php7.php');

class knjd131tModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $detail;

    function knjd131tModel() {
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

        $this->field = array("SEMESTER"      =>  VARS::post("SEMESTER"),
                             "REMARK1"       =>  VARS::post("REMARK1"),
                             "REMARK2"       =>  VARS::post("REMARK2"),
                             "REMARK3"       =>  VARS::post("REMARK3"),
                             "COMMUNICATION" =>  VARS::post("COMMUNICATION"),
                             "UPDATED"       =>  VARS::post("UPDATED")
                             );


        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->exp_grade = VARS::get("GRADE");
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
        
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_H"]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 15;
            $this->getPro["REMARK1"]["gyou"] = 1;
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_H"]);
            $this->getPro["REMARK2"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK2"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK2"]["moji"] = 15;
            $this->getPro["REMARK2"]["gyou"] = 3;
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_H"]);
            $this->getPro["REMARK3"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK3"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK3"]["moji"] = 15;
            $this->getPro["REMARK3"]["gyou"] = 12;
        }
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 25;
            $this->getPro["COMMUNICATION"]["gyou"] = 5;
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(創造学習は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->getPro["REMARK2"]["moji"] * 2)) > $this->getPro["REMARK2"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動は{$this->getPro["REMARK2"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK3"], ($this->getPro["REMARK3"]["moji"] * 2)) > $this->getPro["REMARK3"]["gyou"]) {
            $this->setWarning("MSG915","(部活動、資格取得、検定等は{$this->getPro["REMARK3"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(所見は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }

        return true;
    }

    //更新用モデル
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    //更新
    function &update() {
        knjd131tQuery::getUpdateQuery($this);
        return ;
    }
}
?>
