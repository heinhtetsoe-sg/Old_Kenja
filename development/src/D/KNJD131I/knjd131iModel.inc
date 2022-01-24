<?php

require_once('for_php7.php');
class knjd131iModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $grade_hrclass;
    var $field = array();
    var $selectdata;

    function knjd131iModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("COMMUNICATION"        =>  VARS::post("COMMUNICATION"),        //出欠備考
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),     //担任より
                             "SEMESTER"             =>  VARS::post("SEMESTER")              //学期
                             );

        $this->cmd = VARS::request("cmd");
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
        if (VARS::get("SEMESTER")!="") {
            $this->field["SEMESTER"] = VARS::get("SEMESTER");
        }

        if ($this->cmd == 'replace_update') {
            $this->field["RCHECK1"] = VARS::post("RCHECK1");
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::post("selectdata") != "") {
            $this->selectdata = VARS::post("selectdata");
        } elseif (VARS::post("cmd") != "replace_update") {
            unset($this->selectdata);
        }

        $this->grade    = VARS::get("GRADE");
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
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (10 * 2)) > 10) {
            $this->setWarning("MSG915","( 特別活動の記録は10行までです )");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], (10 * 2)) > 10) {
            $this->setWarning("MSG915","( 通信欄は10行までです )");
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
    
    //部活動・委員会一括更新
    function &getUpdateModel2() {
        $db = Query::dbCheckOut();
        
        $schregArray = explode(",", $this->selectdata);
        foreach ($schregArray as $schregno) {
            $backupsetremark = "";
            $setremark = "";
            $query = knjd131iQuery::getClubCommittee($this, $schregno);
            $result = $db->query($query);
            while ($torikomi = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setremark .= $torikomi["NAME"].'／'.$torikomi["DETAIL_REMARK"]."\r\n";
                //行数チェック
                if ($this->validate_row_cnt($setremark, (10 * 2)) <= 10) {
                    $backupsetremark .= $torikomi["NAME"].'／'.$torikomi["DETAIL_REMARK"]."\r\n";
                } else {
                    $setremark = $backupsetremark;
                    break;
                }
            }
            knjd131iQuery::setInsertQuery($this->field, $setremark, $schregno, $db);
        }
        
        Query::dbCheckIn($db);
        $this->setMessage("MSG201", "特別活動の記録は10行までのため、制限数以降のデータがあった場合は破棄されます。");
    }

    function &update() {
        if ($this->cmd == 'update') {
            knjd131iQuery::getDeleteQuery($this->field, $this->schregno);
            knjd131iQuery::getInsertQuery($this->field, $this->schregno);
        } elseif ($this->cmd == 'replace_update') {
            knjd131iQuery::getInsertQuery2($this->field, $this->selectdata);
        }
        return $result;
    }
}
?>
