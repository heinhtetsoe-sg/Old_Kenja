<?php

require_once('for_php7.php');

class knjd132sModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $detail;

    function knjd132sModel() {
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

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "REMARK1"          =>  VARS::post("REMARK1"),
                             "REMARK2"          =>  VARS::post("REMARK2"),
                             "REMARK3"          =>  VARS::post("REMARK3"),
                             "REMARK4"          =>  VARS::post("REMARK4"),
                             "ATTENDREC_REMARK" =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"          =>  VARS::post("UPDATED")
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
            $this->getPro["REMARK1"]["moji"] = 16;
            $this->getPro["REMARK1"]["gyou"] = 6;
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_H"]);
            $this->getPro["REMARK2"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK2"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK2"]["moji"] = 16;
            $this->getPro["REMARK2"]["gyou"] = 2;
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_H"]);
            $this->getPro["REMARK3"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK3"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK3"]["moji"] = 16;
            $this->getPro["REMARK3"]["gyou"] = 6;
        }

        if ($this->Properties["HREPORTREMARK_DAT_REMARK4_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK4_SIZE_H"]);
            $this->getPro["REMARK4"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK4"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK4"]["moji"] = 16;
            $this->getPro["REMARK4"]["gyou"] = 6;
        }

        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_H"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 16;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 1;
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
            $this->setWarning("MSG915","(学級活動は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->getPro["REMARK2"]["moji"] * 2)) > $this->getPro["REMARK2"]["gyou"]) {
            $this->setWarning("MSG915","(生徒会活動は{$this->getPro["REMARK2"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK3"], ($this->getPro["REMARK3"]["moji"] * 2)) > $this->getPro["REMARK3"]["gyou"]) {
            $this->setWarning("MSG915","(クラブ活動は{$this->getPro["REMARK3"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK4"], ($this->getPro["REMARK4"]["moji"] * 2)) > $this->getPro["REMARK4"]["gyou"]) {
            $this->setWarning("MSG915","(学校行事は{$this->getPro["REMARK4"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
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
        knjd132sQuery::getUpdateQuery($this);
        return ;
    }
}
?>
