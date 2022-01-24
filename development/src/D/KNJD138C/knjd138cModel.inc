<?php

require_once('for_php7.php');

class knjd138cModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $detail;

    function knjd138cModel() {
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
                             "REMARK_01_01"     =>  VARS::post("REMARK_01_01"),
                             "REMARK_01_02"     =>  VARS::post("REMARK_01_02"),
                             "REMARK_01_03"     =>  VARS::post("REMARK_01_03"),
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

        $this->getPro["ATTENDREC_REMARK"]["moji"] = 20;
        $this->getPro["ATTENDREC_REMARK"]["gyou"] = 1;
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
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
        knjd138cQuery::getUpdateQuery($this);
        return ;
    }
}
?>
