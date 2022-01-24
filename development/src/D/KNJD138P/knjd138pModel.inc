<?php

require_once('for_php7.php');
class knjd138pModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $control;

    function knjd138pModel()
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
            unset($this->grade);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),
                             "COMMITTEE"            =>  VARS::post("COMMITTEE"),
                             "REMARK2"              =>  VARS::post("REMARK2"),
                             "CLUB"                 =>  VARS::post("CLUB"),
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

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
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 44;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 2;
        }

        if ($this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_P"]);
            $this->getPro["REMARK2"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK2"]["gyou"] = (int)trim($gyou);
            $this->getPro["COMMITTEE"]["moji"] = (int)trim($moji);
            $this->getPro["COMMITTEE"]["gyou"] = (int)trim($gyou);
            $this->getPro["CLUB"]["moji"] = (int)trim($moji);
            $this->getPro["CLUB"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK2"]["moji"] = 12;
            $this->getPro["REMARK2"]["gyou"] = 3;
            $this->getPro["COMMITTEE"]["moji"] = 12;
            $this->getPro["COMMITTEE"]["gyou"] = 3;
            $this->getPro["CLUB"]["moji"] = 12;
            $this->getPro["CLUB"]["gyou"] = 3;
        }

        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 32;
            $this->getPro["COMMUNICATION"]["gyou"] = 8;
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
            $this->setWarning("MSG915","(総合的な学習の時間所見は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->getPro["REMARK2"]["moji"] * 2)) > $this->getPro["REMARK2"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動のようす(係活動)は{$this->getPro["REMARK2"]["gyou"]}行までです)");
            return false;
        }
        $db = Query::dbCheckOut();
        $grade_cd = $db->getOne(knjd138pQuery::getGradeCd($model));
        Query::dbCheckIn($db);
        if ((int)$grade_cd >= 3) {
            if ($this->validate_row_cnt($this->field["COMMITTEE"], ($this->getPro["COMMITTEE"]["moji"] * 2)) > $this->getPro["COMMITTEE"]["gyou"]) {
                $this->setWarning("MSG915","(特別活動のようす(委員会活動)は{$this->getPro["COMMITTEE"]["gyou"]}行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["CLUB"], ($this->getPro["CLUB"]["moji"] * 2)) > $this->getPro["CLUB"]["gyou"]) {
                $this->setWarning("MSG915","(特別活動のようす(部活動)は{$this->getPro["CLUB"]["gyou"]}行までです)");
                return false;
            }
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(学習のようす所見は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
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
        knjd138pQuery::getInsertQuery($this);
        return ;
    }
}
?>
