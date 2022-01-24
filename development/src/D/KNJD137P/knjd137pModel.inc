<?php

require_once('for_php7.php');
class knjd137pModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $control;

    function knjd137pModel()
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
                             "FOREIGNLANGACT"       =>  VARS::post("FOREIGNLANGACT"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
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
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 16;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 4;
        }

        if ($this->Properties["HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P"]);
            $this->getPro["FOREIGNLANGACT"]["moji"] = (int)trim($moji);
            $this->getPro["FOREIGNLANGACT"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["FOREIGNLANGACT"]["moji"] = 16;
            $this->getPro["FOREIGNLANGACT"]["gyou"] = 4;
        }

        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"]);
            $this->getPro["SPECIALACTREMARK"]["moji"] = (int)trim($moji);
            $this->getPro["SPECIALACTREMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["SPECIALACTREMARK"]["moji"] = 16;
            $this->getPro["SPECIALACTREMARK"]["gyou"] = 4;
        }

        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 15;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 1;
        }

        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 16;
            $this->getPro["COMMUNICATION"]["gyou"] = 6;
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
            $this->setWarning("MSG915","(総合的な学習は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["FOREIGNLANGACT"], ($this->getPro["FOREIGNLANGACT"]["moji"] * 2)) > $this->getPro["FOREIGNLANGACT"]["gyou"]) {
            $this->setWarning("MSG915","(外国語活動は{$this->getPro["FOREIGNLANGACT"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ($this->getPro["SPECIALACTREMARK"]["moji"] * 2)) > $this->getPro["SPECIALACTREMARK"]["gyou"]) {
            $this->setWarning("MSG915","(生活・特別活動のようす所見は{$this->getPro["SPECIALACTREMARK"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出席のようす備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
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
        knjd137pQuery::getInsertQuery($this);
        return ;
    }
}
?>
