<?php

require_once('for_php7.php');

class knja063Model extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;
    var $min_semester;  //最小学期
    var $schregno;      //学籍番号（左リスト）
    var $schoolName;
    var $schoolDiv;

    var $term;
    var $year;
    var $semester;
    var $ghr_cd;
    var $hr_class;

    function knja063Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataLabel  = VARS::post("selectdataLabel");
        //年度・学期
        if (VARS::get("PROGRAMID")){
            unset($this->term);
            unset($this->prgid);
            unset($this->auth);
            unset($this->year);
            unset($this->semester);
            unset($this->ghr_cd);
        }
        $this->term = VARS::post("TERM"); //年度・学期
        if (!strlen($this->term)){
            $this->term = CTRL_YEAR . '-' . CTRL_SEMESTER; //初期値
        }
        $termArray = explode("-", $this->term);
        $this->year     = $termArray[0]; //年度
        $this->semester = $termArray[1]; //学期
        $this->ghr_cd = VARS::post("GHR_CD"); //左クラス
        $this->hr_class = VARS::post("HR_CLASS"); //右クラス
        if (VARS::get("SEND_PRGID") != "") {
            $this->prgid = VARS::get("SEND_PRGID");
            $this->auth = VARS::get("SEND_AUTH");
            $this->year = VARS::get("SEND_YEAR");
            $this->semester = VARS::get("SEND_SEMESTER");
            $this->ghr_cd = VARS::get("SEND_GHR_CD");
            $this->term = $this->year. '-' .$this->semester;
        }
        $this->auth = ($this->auth == "") ? AUTHORITY : $this->auth;
    }

    function validateInput() {
        if (!isset($this->year) || !isset($this->semester) || !isset($this->ghr_cd)) {
            $this->setWarning('MSG301');
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knja063Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
