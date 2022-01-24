<?php

require_once('for_php7.php');
class knjd138hModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $grade_hrclass;
    var $name;
    var $field = array();
    var $control;
    var $selectdata;

    function knjd138hModel()
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

        $this->field = array("TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),
                             "REMARK1"              =>  VARS::post("REMARK1"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),
                             "SEMESTER"             =>  VARS::post("SEMESTER"),
                             "RCHECK1"              =>  VARS::post("RCHECK1"),
                             "RCHECK2"              =>  VARS::post("RCHECK2"),
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

        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_H"]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 13;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 8;
        }

        if ($this->Properties["reportSpecialSize03_01"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize03_01"]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 60;
            $this->getPro["REMARK1"]["gyou"] = 2;
        }

        if ($this->Properties["reportSpecialSize03_02"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize03_02"]);
            $this->getPro["SPECIALACTREMARK"]["moji"] = (int)trim($moji);
            $this->getPro["SPECIALACTREMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["SPECIALACTREMARK"]["moji"] = 15;
            $this->getPro["SPECIALACTREMARK"]["gyou"] = 9;
        }

        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_H"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 25;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 5;
        }

        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 50;
            $this->getPro["COMMUNICATION"]["gyou"] = 2;
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], ($this->getPro["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro["TOTALSTUDYTIME"]["gyou"]) {
            $this->setWarning("MSG915","(学習内容は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(評価は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ($this->getPro["SPECIALACTREMARK"]["moji"] * 2)) > $this->getPro["SPECIALACTREMARK"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動等の記録は{$this->getPro["SPECIALACTREMARK"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出欠席の記録備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(担任欄は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
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
            knjd138hQuery::getInsertQuery($this);
            //追加
            knjd138hQuery::getInsert2Query($this);
            knjd138hQuery::getInsert3Query($this);
        } elseif ($this->cmd == 'replace_update') {
            knjd138hQuery::getInsertQuery2($this, $this->selectdata);
            knjd138hQuery::getInsert2Query2($this, $this->selectdata);
        }

        return ;
    }

}
?>
