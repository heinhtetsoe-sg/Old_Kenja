<?php

require_once('for_php7.php');

class knja080mModel extends Model {
    var $cmd;
    var $leftData;
    var $rightData;
    var $control;
    var $min_semester;  //最小学期
    var $schregno;      //学籍番号（左リスト）
    var $schoolName;
    
    function knja080mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregnoL = array();
        $this->schregnoR = array();
    }

    function init()
    {           
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd        = VARS::request("cmd");
        $this->leftData   = VARS::request("leftData");
        $this->rightData  = VARS::request("rightData");


        if (VARS::request("MIN_SEMESTER")){
            $this->min_semester = VARS::request("MIN_SEMESTER");
        } else {
            $db = Query::dbCheckOut();
            $query = knja080mQuery::getNextYearMinSemes($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $this->min_semester = $row["SEMESTER"];
            Query::dbCheckIn($db);
        }

        $this->year = CTRL_YEAR;
        $this->semester = ((int)CTRL_SEMESTER + 1);
        if (CTRL_SEMESTER == $this->control["学期数"]) {    //最終学期のとき
            $this->year = CTRL_YEAR + 1;
            $this->semester = $this->min_semester;
        }
        $this->year_seme = $this->year."-".$this->semester;
        if (VARS::request("YEAR_SEME")){
            $this->year_seme = VARS::request("YEAR_SEME");

            $tmp = explode("-", $this->year_seme);
            $this->year = $tmp[0];
            $this->semester = $tmp[1];
        }

        if (VARS::post("PROGRAMID")){
            unset($this->leftclass);
            unset($this->rightclass);
        }
        if (VARS::post("LEFT_CLASS")){
            $this->leftclass     = VARS::post("LEFT_CLASS");
        }
        if (VARS::post("RIGHT_CLASS")){
            $this->rightclass     = VARS::post("RIGHT_CLASS");
        }
        if ($this->cmd == "selectYear") {
            unset($this->leftclass);
            unset($this->rightclass);
        }

    }

    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knja080mQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
