<?php

require_once('for_php7.php');
class knjd130dModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();
    var $tutisyoTokubetuKatudo;
    var $grade_hrclass;
    var $selectdata;

    function knjd130dModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
        }
        if (VARS::get("PROGRAMID")!="") {
            unset($this->name);
        }
        $this->field = array("SPECIALACTREMARK" =>  VARS::post("SPECIALACTREMARK"),    //特別活動の記録
                             "TOTALSTUDYTIME"   =>  VARS::post("TOTALSTUDYTIME")       //連絡事項
                             );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }


        if ($this->cmd == 'replace_update') {
            $this->field["RCHECK1"] = VARS::post("RCHECK1");
            $this->field["RCHECK2"] = VARS::post("RCHECK2");
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
        //初期値の設定
        $this->tutisyoTokubetuKatudo = (strlen($this->Properties["tutisyoTokubetuKatudo"])) ? $this->Properties["tutisyoTokubetuKatudo"] : 0;
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->cmd == 'replace_update') {
            if ($this->field["RCHECK1"] == "on") {
                if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (21 * 2)) > 2) {
                    $this->setWarning("MSG915","( 特別活動の記録は2行までです )");
                    return false;
                }
            }
        } else {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (21 * 2)) > 2) {
                $this->setWarning("MSG915","( 特別活動の記録は2行までです )");
                return false;
            }
        }
        if ($this->cmd == 'replace_update') {
            if ($this->field["RCHECK2"] == "on") {
                if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], (21 * 2)) > 7) {
                    $this->setWarning("MSG915","( 連絡事項は7行までです )");
                    return false;
                }
            }
        } else {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], (21 * 2)) > 7) {
                $this->setWarning("MSG915","( 連絡事項は7行までです )");
                return false;
            }
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
            knjd130dQuery::getDeleteQuery($this->schregno);
            knjd130dQuery::getInsertQuery($this->field,$this->schregno);
        } elseif ($this->cmd == 'replace_update') {
            knjd130dQuery::getInsertQuery2($this->field, $this->selectdata);
        }
        return $result;
    }
}
?>
