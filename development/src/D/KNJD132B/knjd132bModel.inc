<?php

require_once('for_php7.php');
class knjd132bModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $control;

    function knjd132bModel() {
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

        $this->field = array(
            "DETAIL_REMARK1"       =>  VARS::post("DETAIL_REMARK1"),
            "DETAIL_REMARK1_CHK"   =>  VARS::post("DETAIL_REMARK1_CHK"),
            "DETAIL_REMARK2"       =>  VARS::post("DETAIL_REMARK2"),
            "DETAIL_REMARK2_CHK"   =>  VARS::post("DETAIL_REMARK2_CHK"),
            "REMARK1"              =>  VARS::post("REMARK1"),
            "SEMESTER"             =>  VARS::post("SEMESTER"),
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

        if (VARS::get("COMMITTEE_REMARK")) {
            $this->committee_remark = VARS::get("COMMITTEE_REMARK");
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
        $db = Query::dbCheckOut();
        $schoolKind = $db->getOne(knjd132bQuery::getSchoolKind($this));
        Query::dbCheckIn($db);

        //特別活動の記録:学級活動(HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_{SCHOOL_KIND})
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$schoolKind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$schoolKind]);
            $this->getPro["DETAIL_REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["DETAIL_REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["DETAIL_REMARK1"]["moji"] = 15;
            $this->getPro["DETAIL_REMARK1"]["gyou"] = 8;
        }
        //特別活動の記録:生徒会活動(HREPORTREMARK_DETAIL_DAT_01_01_REMARK2_SIZE_{SCHOOL_KIND})
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK2_SIZE_".$schoolKind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK2_SIZE_".$schoolKind]);
            $this->getPro["DETAIL_REMARK2"]["moji"] = (int)trim($moji);
            $this->getPro["DETAIL_REMARK2"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["DETAIL_REMARK2"]["moji"] = 15;
            $this->getPro["DETAIL_REMARK2"]["gyou"] = 8;
        }
        //道徳(HREPORTREMARK_DAT_REMARK1_SIZE_{SCHOOL_KIND})
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_".$schoolKind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_".$schoolKind]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 25;
            $this->getPro["REMARK1"]["gyou"] = 5;
        }
        //出欠の記録備考(HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_{SCHOOL_KIND})
        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_".$schoolKind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_".$schoolKind]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 25;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 3;
        }
        //特記すべき事項(HREPORTREMARK_DAT_COMMUNICATION_SIZE_{SCHOOL_KIND})
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$schoolKind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$schoolKind]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 52;
            $this->getPro["COMMUNICATION"]["gyou"] = 11;
        }

    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //特別活動の記録:学級活動
        if ($this->validate_row_cnt($this->field["DETAIL_REMARK1"], ($this->getPro["DETAIL_REMARK1"]["moji"] * 2)) > $this->getPro["DETAIL_REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動の記録　学級活動は{$this->getPro["DETAIL_REMARK1"]["gyou"]}行までです)");
            return false;
        }
        //特別活動の記録:生徒会活動
        if ($this->validate_row_cnt($this->field["DETAIL_REMARK2"], ($this->getPro["DETAIL_REMARK2"]["moji"] * 2)) > $this->getPro["DETAIL_REMARK2"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動の記録　生徒会活動は{$this->getPro["DETAIL_REMARK2"]["gyou"]}行までです)");
            return false;
        }
        //道徳
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(道徳は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        //出欠の記録備考
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
        }
        //特記すべき事項
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(特記すべき事項は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
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
        knjd132bQuery::getInsertQuery($this);
        return ;
    }
}
?>
