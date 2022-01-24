<?php

require_once('for_php7.php');

class knjg070Model extends Model {
    var $semester;
    var $grade;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    function knjg070Model() {
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
                             "ENTRY_DATE"         =>  VARS::post("ENTRY_DATE"),          //記載日
                             "DBNAME"             =>  VARS::post("DBNAME")               //DB変更に備えて
                            );

        if (!$this->field["YEAR"]) {
            $this->field["YEAR"] = CTRL_YEAR;
        }

        if (!$this->field["SDATE"] || !$this->field["EDATE"]) {
            $this->field["SDATE"] = str_replace("-", "/", CTRL_DATE);
            $this->field["EDATE"] = str_replace("-", "/", CTRL_DATE);
        }

        if (!$this->field["ENTRY_DATE"]) {
            $this->field["ENTRY_DATE"] = str_replace("-", "/", CTRL_DATE);
        }



        //学校名取得
        $query = knjg070Query::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);    }
}
?>
