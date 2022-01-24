<?php

require_once('for_php7.php');

class knjd132oModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $detail;

    function knjd132oModel() {
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

        $this->field = array("SEMESTER"                 =>  VARS::post("SEMESTER"),
                             "REMARK1"                  =>  VARS::post("REMARK1"),
                             "REMARK2"                  =>  VARS::post("REMARK2"),
                             "REMARK3"                  =>  VARS::post("REMARK3"),
                             "REMARK4"                  =>  VARS::post("REMARK4"),
                             "REMARK5"                  =>  VARS::post("REMARK5"),
                             "COMMUNICATION"            =>  VARS::post("COMMUNICATION"),
                             "ATTENDREC_REMARK"         =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"                  =>  VARS::post("UPDATED")
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
        
        if (VARS::get("sendSEME") != "") {
            $this->ikkatsuSeme = VARS::get("sendSEME");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        $moji = 10;
        $gyou = 3;
        if ($this->validate_row_cnt($this->field["REMARK1"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(学級活動は{$gyou}行までです)");
            return false;
        }
        
        $moji = 20;
        $gyou = 3;
        if ($this->validate_row_cnt($this->field["REMARK2"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(生徒会活動は{$gyou}行までです)");
            return false;
        }

        $moji = 10;
        $gyou = 3;
        if ($this->validate_row_cnt($this->field["REMARK3"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(部活動の成果は{$gyou}行までです)");
            return false;
        }

        $moji = 20;
        $gyou = 4;
        if ($this->validate_row_cnt($this->field["REMARK4"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(資格取得・検定等は{$gyou}行までです)");
            return false;
        }

        $moji = 10;
        $gyou = 3;
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(資格取得・検定等は{$gyou}行までです)");
            return false;
        }

        $moji = 40;
        $gyou = 2;
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(備考は{$gyou}行までです)");
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
        knjd132oQuery::getUpdateQuery($this);
        return ;
    }

}

?>
