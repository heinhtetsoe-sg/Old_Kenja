<?php

require_once('for_php7.php');

class knja143bModel extends Model {
    var $semester;
    var $grade;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    function knja143bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("DATE"               =>  VARS::post("DATE"),                //学籍処理日
                             "YEAR"               =>  VARS::post("YEAR"),                //年度
                             "SINNYU_TENNYU"      =>  VARS::post("SINNYU_TENNYU"),       //1:新入生 2:転入生、編入生
                             "SDATE"              =>  VARS::post("SDATE"),               //入学年月日(開始)
                             "EDATE"              =>  VARS::post("EDATE"),               //入学年月日(終了)
                             "DBNAME"             =>  VARS::post("DBNAME")               //DB変更に備えて
                            );

        if (!$this->field["YEAR"]) {
            $this->field["YEAR"] = CTRL_YEAR;
        }

        if (!$this->field["SDATE"] || !$this->field["EDATE"]) {
            $this->field["SDATE"] = str_replace("-", "/", CTRL_DATE);
            $this->field["EDATE"] = str_replace("-", "/", CTRL_DATE);
        }

        $this->getPropertiesAll();

        //学校名取得
        $query = knja143bQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);    }
}
?>
