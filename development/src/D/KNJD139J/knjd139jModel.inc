<?php

require_once('for_php7.php');
class knjd139jModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $grade_hrclass;
    var $field = array();

    function knjd139jModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("REMARK1"              =>  VARS::post("REMARK1"),          //部活動
                             "REMARK2"              =>  VARS::post("REMARK2"),          //表彰
                             "SEMESTER"             =>  VARS::post("SEMESTER"),         //学期
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"), //特別活動の記録
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"), //出席の記録備考
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION")     //学級担任の所見
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }

        $this->grade    = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_J"]);
            $this->remark1_moji = (int)trim($moji);
            $this->remark1_gyou = (int)trim($gyou);
        } else {
            $this->remark1_moji = 13;   //デフォルトの値
            $this->remark1_gyou = 2;    //デフォルトの値
        }
        if ($this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_J"]);
            $this->remark2_moji = (int)trim($moji);
            $this->remark2_gyou = (int)trim($gyou);
        } else {
            $this->remark2_moji = 13;   //デフォルトの値
            $this->remark2_gyou = 2;    //デフォルトの値
        }
        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 13;  //デフォルトの値
            $this->specialactremark_gyou = 2;   //デフォルトの値
        }
        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 28;  //デフォルトの値
            $this->attendrec_remark_gyou = 1;   //デフォルトの値
        }
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]);
            $this->communication_moji = (int)trim($moji);
            $this->communication_gyou = (int)trim($gyou);
        } else {
            $this->communication_moji = 23;     //デフォルトの値
            $this->communication_gyou = 5;      //デフォルトの値
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数チェック
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->remark1_moji * 2)) > $this->remark1_gyou) {
            $this->setWarning("MSG915","(部活動は{$this->remark1_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->remark2_moji * 2)) > $this->remark2_gyou) {
            $this->setWarning("MSG915","(表彰は{$this->remark2_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ($this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
            $this->setWarning("MSG915","(特別活動の記録は{$this->specialactremark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915","(出席の記録備考は{$this->attendrec_remark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->communication_moji * 2)) > $this->communication_gyou) {
            $this->setWarning("MSG915","(学級担任の所見は{$this->communication_gyou}行までです)");
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
        //通年
        knjd139jQuery::getInsertUpdateQuery($this, $this->schregno, "9");
        //学期
        knjd139jQuery::getInsertUpdateQuery($this, $this->schregno, $this->field["SEMESTER"]);

        return;
    }
}
?>
