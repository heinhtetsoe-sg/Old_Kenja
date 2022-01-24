<?php

require_once('for_php7.php');
class knjd155rModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $grade_hrclass;
    var $name;
    var $field = array();
    var $control;
    var $selectdata;

    function knjd155rModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        if (VARS::post("selectdata") != "") {
            $this->selectdata = VARS::post("selectdata");
        } elseif (VARS::post("cmd") != "replace_update") {
            unset($this->selectdata);
        }

        $this->field = array("REMARK1"              =>  VARS::post("REMARK1"),
                             "REMARK2"              =>  VARS::post("REMARK2"),
                             "SEMESTER"             =>  VARS::post("SEMESTER"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("cmd") == "replace") {
            $this->grade_hrclass = VARS::request("GRADE_HRCLASS");
        }

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
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
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->getPro["REMARK1"]["moji"] = 15;
        $this->getPro["REMARK1"]["gyou"] = 13;

        $this->getPro["REMARK2"]["moji"] = 9;
        $this->getPro["REMARK2"]["gyou"] = 5;
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(担任所見は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->getPro["REMARK2"]["moji"] * 2)) > $this->getPro["REMARK2"]["gyou"]) {
            $this->setWarning("MSG915","(その他は{$this->getPro["REMARK2"]["gyou"]}行までです)");
            return false;
        }

        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        if ($this->cmd == 'update') {
            knjd155rQuery::getInsertQuery($this);
        }

        return ;
    }

}
?>
