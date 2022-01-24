<?php

require_once('for_php7.php');

class knjz065mModel extends Model {
    var $cmd;
    var $ctrl;
    var $year;
    var $semester;
    var $gakubu_school_kind = "";
    var $ghr_cd = "";
    var $grade = "";
    var $hr_class = "";
    var $condition = "";
    var $groupcd = "";
    var $set_subclasscd = "";
    var $unitcd = "";
    var $unitname = "";
    var $field;

    function knjz065mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->semester);
            unset($this->gakubu_school_kind);
            unset($this->ghr_cd);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->condition);
            unset($this->groupcd);
            unset($this->set_subclasscd);
            unset($this->unitcd);
            $this->field = array();
        }
        $this->year = CTRL_YEAR;
        $this->semester = "9";
        $this->ghr_cd = "00";
        $this->grade = "00";
        $this->hr_class = "000";

        if (VARS::post("GAKUBU_SCHOOL_KIND") != "") {
            $this->gakubu_school_kind = VARS::post("GAKUBU_SCHOOL_KIND");
        }

        if (VARS::get("CONDITION") != "") {
            $this->condition = VARS::get("CONDITION");
        }
        if (VARS::get("GROUPCD") != "") {
            $this->groupcd = VARS::get("GROUPCD");
        }
        if (VARS::get("SET_SUBCLASSCD") != "") {
            $this->set_subclasscd = VARS::get("SET_SUBCLASSCD");
        }
        if (VARS::get("UNITCD") != "") {
            $this->unitcd = VARS::get("UNITCD");
        }

        $this->field = array(
                             "CONDITION"          =>     VARS::post("CONDITION"),             //状態区分
                             "GROUPCD"            =>     VARS::post("GROUPCD"),               //科目グループ名
                             "SET_SUBCLASSCD"     =>     VARS::post("SET_SUBCLASSCD"),        //科目
                             "UNITCD"             =>     VARS::post("UNITCD"),                //単元コード
                             "UNITNAME"           =>     VARS::post("UNITNAME"),              //単元名
                            );

        $this->cmd = VARS::request("cmd");

    }

    //入力項目チェック
    function validateInput() {
    
        $db = Query::dbCheckOut();
        if ($this->cmd == "insert" || $this->cmd == "update" || $this->cmd == "delete") {
            //文字数チェック
            if (strlen($this->field["UNITCD"]) > 2 ) {
                $this->setWarning("MSG915", "(単元コード) 2バイトまで");
                return false;
            } else if (strlen($this->field["UNITNAME"]) > 60 ) {
                $this->setWarning("MSG915", "(単元名) 全角20文字まで");
                return false;
            }
            //更新チェック
            if ($this->cmd == "update" || $this->cmd == "delete") {
                if ($this->condition != $this->field["CONDITION"] || $this->groupcd != $this->field["GROUPCD"] || $this->set_subclasscd != $this->field["SET_SUBCLASSCD"] || $this->unitcd != $this->field["UNITCD"]) {
                    $this->setWarning("MSG304");
                    return false;
                }
            }
            //登録チェック
            if ($this->cmd == "insert") {
                $cnt = $db->getOne(knjz065mQuery::getUnitGroupYmst($this, $this->ghr_cd, $this->grade, $this->hr_class, $this->field["CONDITION"], $this->field["GROUPCD"], $this->field["SET_SUBCLASSCD"], $this->field["UNITCD"]));
                if ($cnt > 0) {
                    $this->setWarning("MSG302");
                    return false;
                }
            }
        } else if ($this->cmd == "copy") {
            //存在チェック（指定年度） -- GRADE_KIND_COMP_GROUP_DAT
            $cnt_comp_this = $db->getOne(knjz065mQuery::checkCompGroupDat($this->year));
            if ($cnt_comp_this == 0) {
                $this->setWarning("MSG305");
                return false;
            }
            //存在チェック（前年度） -- GRADE_KIND_UNIT_GROUP_YMST
            $cnt_unit_pre = $db->getOne(knjz065mQuery::checkUnitGroupDat(intval($this->year - 1)));
            if ($cnt_unit_pre == 0) {
                $this->setWarning("MSG917","対象データが存在しません。");
                return false;
            }
            //存在チェック（指定年度） -- GRADE_KIND_UNIT_GROUP_YMST
            $cnt_unit_this = $db->getOne(knjz065mQuery::checkUnitGroupDat($this->year));
            if ($cnt_unit_this > 0) {
                $this->setWarning("MSG917","指定年度にデータが存在します。");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz065mQuery::UpdateQuery($this);
            //更新時にグループコードをセット
            if ($this->cmd != "delete") {
                $this->condition = $this->field["CONDITION"];
                $this->groupcd = $this->field["GROUPCD"];
                $this->set_subclasscd = $this->field["SET_SUBCLASSCD"];
                $this->unitcd = $this->field["UNITCD"];
            } else {
                $this->field = array();
            }
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz065mQuery::UpdateQuery($this);
            //更新時にグループコードをセット
            if ($this->selectdata) {
                $this->condition = "";
                $this->groupcd = "";
                $this->set_subclasscd = "";
                $this->unitcd = "";
            } else {
                $this->field = array();
            }
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $result = $db->query(knjz065mQuery::copyUnitGroupYmst($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjz065mQuery::copyInsertQuery($this, $row));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
