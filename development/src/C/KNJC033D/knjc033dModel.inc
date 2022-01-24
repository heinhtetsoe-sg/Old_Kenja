<?php

require_once('for_php7.php');

class knjc033dModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $grade;
    var $hr_class;
    var $di_cd;
    var $grade_hr_class;
    var $admin_control;
    var $use_di_cd;
    var $auth;

    function knjc033dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->grade);
            unset($this->hr_class);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID")) {
            $this->useDateText = "";
            $this->cntl_dt_key = "";
            $this->grade_hr_class = "";
        }

        if ($this->cmd == "KNJC032D") {
            $this->useDateText = "1";
            $this->cntl_dt_key = "";
        }

        if (VARS::request("DATE")) {
            $this->cntl_dt_key = VARS::request("DATE");
        }
        $this->befDate = VARS::request("befDate");

        //権限
        $this->auth = ($this->useDateText) ? common::SecurityCheck(STAFFCD,"KNJC032D") : AUTHORITY;

        if(VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if(VARS::get("HR_CLASS") != "") {
            $this->hr_class = VARS::get("HR_CLASS");
        }

        if ($this->cmd != "") {
            $this->grade_hr_class = VARS::post("GRADE_HR_CLASS");
        }

        $this->di_cd    = VARS::post("DI_CD");
        $this->schregno = VARS::post("SCHREGNO");

        if ($this->schregno) {
            $schregno = explode(',', $this->schregno);
            for ($i=0; $i < get_count($schregno); $i++) {
                $this->field[$schregno[$i]]["ATTEND1"] = VARS::post("ATTEND1_".$schregno[$i]);
                $this->field[$schregno[$i]]["ATTEND2"] = VARS::post("ATTEND2_".$schregno[$i]);
                $this->field[$schregno[$i]]["ATTEND3"] = VARS::post("ATTEND3_".$schregno[$i]);
            }
        }

        //使用する出欠コード（出欠コード／集計フィールド名、表示枠番号）
        $this->use_di_cd = array("1"    => array("ABSENT",      "1"),
                                 "2"    => array("SUSPEND",     "1"),
                                 "3"    => array("MOURNING",    "1"),
                                 "6"    => array("NONOTICE",    "1"),
                                 "15"   => array("LATE",        "2"),
                                 "16"   => array("EARLY",       "3")
                                );
    }

    //更新処理（全員出席）
    function &getUpdateModel() {
        knjc033dQuery::getUpdateQuery($this);
        unset($this->grade_hr_class);
        return;
    }

    //更新処理（出欠入力）
    function &getUpdateModel2() {
        knjc033dQuery::getUpdateQuery2($this);
        return;
    }

    //削除処理（全てクリア）
    function &getDeleteModel2() {
        knjc033dQuery::getDeleteQuery2($this);
        $this->setMessage("MSG201");

        return;
    }
}
?>
