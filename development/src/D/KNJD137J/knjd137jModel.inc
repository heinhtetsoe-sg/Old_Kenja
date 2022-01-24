<?php

require_once('for_php7.php');
class knjd137jModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;

    function knjd137jModel()
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
            unset($this->exp_grade);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );
                             
        $this->field["RECORD_VAL01"] = VARS::post("RECORD_VAL01");
        $this->field["RECORD_VAL02"] = VARS::post("RECORD_VAL02");

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

        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J"]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 16;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 7;
        }

        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 15;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 1;
        }

        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 16;
            $this->getPro["COMMUNICATION"]["gyou"] = 7;
        }
        
        if ($this->Properties["reportSpecialSize03_02"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize03_02"]);
            $this->getPro["RECORD_VAL02"]["moji"] = (int)trim($moji);
            $this->getPro["RECORD_VAL02"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["RECORD_VAL02"]["moji"] = 16;
            $this->getPro["RECORD_VAL02"]["gyou"] = 7;
        }

        if ($this->Properties["reportSpecialSize03_01"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize03_01"]);
            $this->getPro["RECORD_VAL01"]["moji"] = (int)trim($moji);
            $this->getPro["RECORD_VAL01"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["RECORD_VAL01"]["moji"] = 10;
            $this->getPro["RECORD_VAL01"]["gyou"] = 3;
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
            $this->setWarning("MSG915","(総合的な学習の時間は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(担任からの所見は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["RECORD_VAL02"], ($this->getPro["RECORD_VAL02"]["moji"] * 2)) > $this->getPro["RECORD_VAL02"]["gyou"]) {
            $this->setWarning("MSG915","(観点は{$this->getPro["RECORD_VAL02"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["RECORD_VAL01"], ($this->getPro["RECORD_VAL01"]["moji"] * 2)) > $this->getPro["RECORD_VAL01"]["gyou"]) {
            $this->setWarning("MSG915","(身体の記録は{$this->getPro["RECORD_VAL01"]["gyou"]}行までです)");
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
        knjd137jQuery::getInsertQuery($this);
        return ;
    }
}
?>
