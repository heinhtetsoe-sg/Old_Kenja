<?php

require_once('for_php7.php');

class knjg105aModel extends Model {

    var $field = array();
    var $data  = array();
    var $checked     = array();
    var $baseRemark1 = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjg105aModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field   = array();
            $this->checked = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DATE"             =>  VARS::post("DATE"),             //集計日付
                             "SICK_CNT"         =>  VARS::post("SICK_CNT"),         //欠席
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        foreach ((array)$this->data["SCHREGNO"] as $schregno) {
            $this->field["BASE_REMARK1{$schregno}"] = VARS::post("BASE_REMARK1".$schregno);
        }

        $this->cmd     = VARS::request("cmd");

        $this->checkSch     = VARS::post("checkSch");
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["GRADE_HR_CLASS"] == "") {
            $this->setWarning("MSG304", "( 年組 )");
            return false;
        }
        //文字数チェック
        foreach ((array)$this->checked as $schregno) {
            if ($this->validate_row_cnt($this->field["BASE_REMARK1{$schregno}"], (12 * 2)) > 2) {
                $this->setWarning("MSG915","(主な理由は２行までです)");
                $this->checked = array();
                return false;
            }
        }

        //桁数チェック
        foreach ((array)$this->checked as $schregno) {
            if (strlen($this->field["BASE_REMARK1{$schregno}"]) > 768) {
                $this->setWarning("MSG915","( 主な理由は768バイトまでです )");
                $this->checked = array();
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        foreach ((array)$this->data["SCHREGNO"] as $schregno) {
            if (VARS::request("CHECKED".$schregno)) {
                $this->checked[$schregno]     = VARS::request("CHECKED".$schregno);     //チェック
            }
        }

        if ($this->validateInput()) {
            knjg105aQuery::update($this);
            $this->checked = array();
            $this->setMessage("MSG201");
        }
    }

    //年度内の処理か？チェック
    function checkCtrlDay($day) {
        $day = str_replace("/","-",$day);

        //年度内の処理に限る
        if ((CTRL_YEAR."-04-01" <= $day && $day < (CTRL_YEAR + 1)."-04-01")) {
            return true;
        } else {
            $this->setWarning("MSG915","(処理年度範囲外)");
            return false;
        }

    }
}
//var_dump($_REQUEST);
?>
