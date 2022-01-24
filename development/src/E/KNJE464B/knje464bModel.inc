<?php

require_once('for_php7.php');

class knje464bModel extends Model {
    var $cmd;
    var $ctrl;
    var $year;
    var $schoolKind;
    var $facilityGrp = "";
    var $field;
    var $selectdata = "";
    var $selectdata2 = "";

    function knje464bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->hr_class);
            unset($this->hr_class2);
            unset($this->schoolKind);
            unset($this->facilityGrp);
            $this->field = array();
        }
        $this->year = CTRL_YEAR;
        if (VARS::get("SPRT_FACILITY_GRP") != "") {
            $this->facilityGrp = VARS::get("SPRT_FACILITY_GRP");
        }

        $this->field = array("IPT_FACILITY_GRP"     =>  (VARS::get("SPRT_FACILITY_GRP") != "") ? $this->facilityGrp : ((VARS::post("IPT_FACILITY_GRP") != "") ? sprintf("%03d", VARS::post("IPT_FACILITY_GRP")) : ""),     //グループコード
                             "IPT_GROUPNAME"        =>  VARS::post("IPT_GROUPNAME"),        //グループ名
                             "SUPPORTFACILITY"      =>  VARS::post("SUPPORTFACILITY"),      //支援機関
                            );
        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");

        $this->selectdata = VARS::post("selectdata");
        $this->selectdata2 = VARS::post("selectdata2");

        $this->schoolKind = VARS::post("SCHOOL_KIND");
        $this->hr_class = VARS::post("GRADE_HR_CLASS");
        $this->cmd = VARS::request("cmd");

        $db = Query::dbCheckOut();
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
    
        $db = Query::dbCheckOut();
        if ($this->cmd == "update" || $this->cmd == "update2" || $this->cmd == "delete") {
            //必須入力チェック
            if (!$this->field["IPT_FACILITY_GRP"] || !$this->field["IPT_GROUPNAME"]) {
                $this->setWarning("MSG304");
                $this->groupcd = "";
                return false;
            }
            if ($this->cmd == "update2") {
                if ($this->hr_class == "" || $this->schoolKind == "") {
                    $this->setWarning("MSG304");
                    return false;
                }
            }
            //文字数チェック
            if (strlen($this->field["IPT_FACILITY_GRP"]) > 3 ) {
                $this->setWarning("MSG915", "(グループコード) 3バイトまで");
                return false;
            } else if (strlen($this->field["IPT_GROUPNAME"]) > 30 ) {
                $this->setWarning("MSG915", "(名称) 30バイトまで");
                return false;
            }
            if ($this->cmd != "delete") {
                //更新チェック
                if ($this->facilityGrp != $this->field["IPT_FACILITY_GRP"]) {
                    $getData = $db->getOne(knje464bQuery::getList($this, $this->field["IPT_FACILITY_GRP"]));
                    if ($getData > 0) {
                        $this->setWarning("MSG308");
                        $this->groupcd = "";
                        return false;
                    }
                }
            }
        } else if ($this->cmd == "copy") {
            //存在チェック（前年度）
            $cnt_preYM = $db->getOne(knje464bQuery::chkChallengedSupportFacilityGrpMst($this, intval($this->year - 1)));
            $cnt_preD  = $db->getOne(knje464bQuery::chkChallengedSupportFacilityGrpDat($this, intval($this->year - 1)));
            if ($cnt_preYM == 0 || $cnt_preD == 0) {
                $this->setWarning("MSG917","対象データが存在しません。");
                return false;
            }

            //存在チェック（指定年度）
            $cnt_thisYM = $db->getOne(knje464bQuery::chkChallengedSupportFacilityGrpMst($this, $this->year));
            $cnt_thisD  = $db->getOne(knje464bQuery::chkChallengedSupportFacilityGrpDat($this, $this->year));
            if ($cnt_thisYM > 0 || $cnt_thisD > 0) {
                $this->setWarning("MSG917","指定年度にデータが存在します。");
                return false;
            }
        } else if ($this->cmd == "copy2") {
            //必須入力チェック
            if (!$this->field["IPT_FACILITY_GRP"] || !$this->field["IPT_GROUPNAME"]) {
                $this->setWarning("MSG304");
                $this->groupcd = "";
                return false;
            }
            //更新チェック
            if ($this->facilityGrp != $this->field["IPT_FACILITY_GRP"]) {
                $getData = $db->getOne(knje464bQuery::getList($this, $this->field["IPT_FACILITY_GRP"]));
                if ($getData > 0) {
                    $this->setWarning("MSG308");
                    $this->groupcd = "";
                    return false;
                }
            }
            //存在チェック（前年度）
            $query = knje464bQuery::chkSchregChallengedSupportFacilityGrpDat($this, intval($this->year - 1));
            $cnt_pre = $db->getOne($query);
            if ($cnt_pre == 0) {
                $this->setWarning("MSG917","対象データが存在しません。");
                return false;
            }
            //存在チェック（指定年度）
            $query = knje464bQuery::chkSchregChallengedSupportFacilityGrpDat($this, $this->year);
            $cnt_this = $db->getOne($query);
            if ($cnt_this) {
                $this->setWarning("MSG917","指定年度にデータが存在します。");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    //更新処理支援機関用
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knje464bQuery::UpdateQuery($this);
            //$this->getUpdateModel2();
            //更新時にグループコードをセット
            if ($this->cmd != "delete") {
                $this->facilityGrp = $this->field["IPT_FACILITY_GRP"];
            } else {
                $this->field = array();
            }
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理生徒用
    function &getUpdateModel2() {
        if ($this->validateInput()) {
            knje464bQuery::UpdateQueryAim($this);
            //更新時にグループコードをセット
            if ($this->selectdata2) {
                $this->facilityGrp = $this->field["IPT_FACILITY_GRP"];
            }
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $key = "";
            if ($this->cmd == "copy") {
                $query = knje464bQuery::copyChallengedSupportFacilityGrpMst($this);
                $db->query($query);
                $query = knje464bQuery::copyChallengedSupportFacilityGrpDat($this);
                $db->query($query);
            } else if ($this->cmd == "copy2") {
                $query = knje464bQuery::copySchregChallengedSupportFacilityGrpDat($this);
                $db->query($query);
            }

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }

    function &getCopyModel2() {
    }

    //削除処理
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knje464bQuery::UpdateQuery($this);
            knje464bQuery::UpdateQueryAim($this);
            $this->groupcd = "";
            $this->selectdata = "";
            $this->selectdata2 = "";
            $this->field = array();
            $this->schoolKind = "";
            $this->hr_class = "";
            $this->setMessage("MSG201");
        }
        return;
    }


}
?>
