<?php

require_once('for_php7.php');
class knjd137iModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $control;

    function knjd137iModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        $this->field = array("PRINT_SEMESTER"       =>  VARS::post("PRINT_SEMESTER"),
                             "FOREIGNLANGACT"       =>  VARS::post("FOREIGNLANGACT"),
                             "TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "REMARK3"        =>  VARS::post("REMARK3"),
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

        /****************************/
        /* 文字数・行数の初期値設定 */
        /****************************/
        if ($this->Properties["HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P"]);
            $this->getPro["FOREIGNLANGACT"]["moji"] = (int)trim($moji);
            $this->getPro["FOREIGNLANGACT"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["FOREIGNLANGACT"]["moji"] = 16;
            $this->getPro["FOREIGNLANGACT"]["gyou"] = 5;
        }

        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 16;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 5;
        }

        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 15;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 1;
        }

        if ($this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_P"]);
            $this->getPro["REMARK3"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK3"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK3"]["moji"] = 16;
            $this->getPro["REMARK3"]["gyou"] = 10;
        }

        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 16;
            $this->getPro["COMMUNICATION"]["gyou"] = 10;
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["FOREIGNLANGACT"], ($this->getPro["FOREIGNLANGACT"]["moji"] * 2)) > $this->getPro["FOREIGNLANGACT"]["gyou"]) {
            $this->setWarning("MSG915","(外国語活動の記録は{$this->getPro["FOREIGNLANGACT"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], ($this->getPro["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro["TOTALSTUDYTIME"]["gyou"]) {
            $this->setWarning("MSG915","(総合的な学習の時間は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出席のようす備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK3"], ($this->getPro["REMARK3"]["moji"] * 2)) > $this->getPro["REMARK3"]["gyou"]) {
            $this->setWarning("MSG915","(自立活動は{$this->getPro["REMARK3"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(学校からは{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjd137iQuery::getInsertQuery($this);
        return ;
    }
}
?>
