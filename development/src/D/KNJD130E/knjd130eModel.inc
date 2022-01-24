<?php

require_once('for_php7.php');
class knjd130eModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();
    var $grade_hrclass;
    var $selectdata;

    function knjd130eModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
        }
        if (VARS::get("PROGRAMID")!="") {
            unset($this->name);
        }
        $this->field = array("TOTALSTUDYTIME"   => VARS::post("TOTALSTUDYTIME"),    //学習内容
                             "SPECIALACTREMARK" => VARS::post("SPECIALACTREMARK"),  //評価
                             "COMMUNICATION"    => VARS::post("COMMUNICATION")      //備考
                            );

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == 'replace_update') {
            $this->field["RCHECK1"] = VARS::post("RCHECK1");
            $this->field["RCHECK2"] = VARS::post("RCHECK2");
            $this->field["RCHECK3"] = VARS::post("RCHECK3");
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        if (VARS::post("selectdata") != "") {
            $this->selectdata = VARS::post("selectdata");
        } elseif (VARS::post("cmd") != "replace_update") {
            unset($this->selectdata);
        }

        $this->grade = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
        if (VARS::get("cmd") == "replace") {
            $this->grade_hrclass = VARS::request("GRADE_HRCLASS");
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYTIME"]) > 316) {
            $this->setWarning("MSG915","( 学習内容は316バイトまでです )");
            return false;
        }
        if (strlen($this->field["SPECIALACTREMARK"]) > 316) {
            $this->setWarning("MSG915","( 評価は316バイトまでです )");
            return false;
        }
        if (strlen($this->field["COMMUNICATION"]) > 323) {
            $this->setWarning("MSG915","( 備考は323バイトまでです )");
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
        if ($this->cmd == 'update') {
            knjd130eQuery::getDeleteQuery($this->schregno);
            knjd130eQuery::getInsertQuery($this->field,$this->schregno);
        } elseif ($this->cmd == 'replace_update') {
            knjd130eQuery::getInsertQuery2($this->field, $this->selectdata);
        }
        return $result;
    }
}
?>
