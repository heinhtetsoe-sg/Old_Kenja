<?php

require_once('for_php7.php');

class knjz065Model extends Model {
    var $cmd;
    var $ctrl;
    var $year;
    var $semester = "";
    var $regdSemester = "";
    var $gakubu_school_kind = "";
    var $hukusiki_radio = "";
    var $hr_class = "";
    var $hr_class2 = "";
    var $condition = "";
    var $groupcd = "";
    var $unitcd = "";
    var $set_subclasscd = "";
    var $field;
    var $selectdata = "";

    function knjz065Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->semester);
            unset($this->regdSemester);
            unset($this->gakubu_school_kind);
            unset($this->hukusiki_radio);
            unset($this->hr_class);
            unset($this->hr_class2);
            unset($this->condition);
            unset($this->groupcd);
            unset($this->unitcd);
            unset($this->set_subclasscd);
            $this->field = array();
        }
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if ($this->Properties["useGradeKindCompGroupSemester"] == "1") {
            if (VARS::post("SEMESTER") != "") {
                $this->semester = VARS::request("SEMESTER");
                $this->regdSemester = VARS::request("SEMESTER");
            }
        } else {
            $this->semester = "9";
            $this->regdSemester = CTRL_SEMESTER;
        }
        if (VARS::post("GAKUBU_SCHOOL_KIND") != "") {
            $this->gakubu_school_kind = VARS::request("GAKUBU_SCHOOL_KIND");
        }
        if ($this->Properties["useSpecial_Support_School"] == '1') {
            if (VARS::post("HUKUSIKI_RADIO") != "") {
                $this->hukusiki_radio = VARS::request("HUKUSIKI_RADIO");
            }
        }
        if (VARS::post("HR_CLASS") != "") {
            $this->hr_class = VARS::request("HR_CLASS");
        }
        if (VARS::post("HR_CLASS2") != "") {
            $this->hr_class2 = VARS::request("HR_CLASS2");
        }
        if (VARS::get("CONDITION") != ""){
            $this->condition = VARS::get("CONDITION");
        }
        if (VARS::get("GROUPCD") != "") {
            $this->groupcd = VARS::get("GROUPCD");
        }
        if (VARS::get("UNITCD") != "") {
            $this->unitcd = VARS::get("UNITCD");
        }
        if (VARS::get("SET_SUBCLASSCD") != "") {
            $this->set_subclasscd = VARS::get("SET_SUBCLASSCD");
        }

        $this->field = array("UNITCD"       =>  (VARS::post("UNITCD") == "") ? "" : sprintf("%02d", VARS::post("UNITCD")),       //単元コード
                             "UNITNAME"     =>  VARS::post("UNITNAME"),     //単元名
                             "UNITABBV"     =>  VARS::post("UNITABBV"),     //単元名略称
                            );

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");

        //初期値設定
        $db = Query::dbCheckOut();
        //学部
        $gakubu_school_kind = $db->getOne(knjz065Query::getSchoolKind());
        $this->gakubu_school_kind = ($this->gakubu_school_kind) ? $this->gakubu_school_kind : $gakubu_school_kind;
        //年組コンボ切替
        $this->hukusiki_radio = ($this->hukusiki_radio != "") ? $this->hukusiki_radio : 1;
        //年組
        $hr_class = $db->getOne(knjz065Query::getHrClass($this));
        $this->hr_class = ($this->hr_class != "") ? $this->hr_class : $hr_class;
        //年組（特別クラス選択時）
        $hr_class2 = $db->getOne(knjz065Query::getHrClass2($this));
        $this->hr_class2 = ($this->hr_class2 != "") ? $this->hr_class2 : (($this->hr_class2 == "") ? '00-000' : $hr_class2);
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
    
        $db = Query::dbCheckOut();
        if ($this->cmd == "update" || $this->cmd == "delete") {
            //必須入力チェック
            if (!$this->gakubu_school_kind || !$this->hr_class || !$this->hr_class2 || !$this->condition || !$this->groupcd || !$this->field["UNITCD"] || !$this->field["UNITNAME"]) {
                $this->setWarning("MSG304");
                return false;
            }
        }
        if ($this->cmd == "update") {
            //文字数チェック
            if (strlen($this->field["UNITCD"]) > 2 ) {
                $this->setWarning("MSG915", "(単元コード) 2バイトまで");
                return false;
            } else if (strlen($this->field["UNITNAME"]) > 180 ) {
                $this->setWarning("MSG915", "(単元名（指導目標）) 180バイトまで");
                return false;
            } else if (strlen($this->field["UNITABBV"]) > 90 ) {
                $this->setWarning("MSG915", "(略称) 90バイトまで");
                return false;
            }
        } else if ($this->cmd == "copy") {
            //存在チェック（指定年度） -- GRADE_KIND_COMP_GROUP_DAT
            $cnt_comp_this = $db->getOne(knjz065Query::checkCompGroupDat($model, $this->year));
            if ($cnt_comp_this == 0) {
                $this->setWarning("MSG305");
                return false;
            }
            //存在チェック（前年度） -- GRADE_KIND_UNIT_GROUP_YMST
            $cnt_unit_pre = $db->getOne(knjz065Query::checkUnitGroupDat($model, intval($this->year - 1)));
            if ($cnt_unit_pre == 0) {
                $this->setWarning("MSG917","対象データが存在しません。");
                return false;
            }
            //存在チェック（指定年度） -- GRADE_KIND_UNIT_GROUP_YMST
            $cnt_unit_this = $db->getOne(knjz065Query::checkUnitGroupDat($model, $this->year));
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
            knjz065Query::UpdateQuery($this);
            //更新時にグループコードをセット
            if ($this->cmd == "delete") {
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
            $result = $db->query(knjz065Query::copyUnitGroupYMst($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjz065Query::copyInsertQuery($this, $row));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
