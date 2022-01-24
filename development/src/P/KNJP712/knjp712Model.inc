<?php

require_once('for_php7.php');

class knjp712Model extends Model {
    var $field = array();
    var $cmd;
    var $schdiv;
    var $year;
    var $schoolkind;
    var $collect_grp_cd;

    function knjp712Model() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->schdiv);
            unset($this->year);
            unset($this->schoolkind);
            unset($this->collect_grp_cd);
            unset($this->grade_cm);
            unset($this->coursecode);
            unset($this->grade_hr_class);
            unset($this->clubcd);
            unset($this->domicd);
            $this->field = array();
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHDIV"               =>  VARS::post("SCHDIV"),
                             "SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND"),
                             "COLLECT_GRP_CD"       =>  VARS::post("COLLECT_GRP_CD"),
                             "COLLECT_PATTERN_CD"   =>  VARS::post("COLLECT_PATTERN_CD"),
                             "SLIP_DATE"            =>  VARS::post("SLIP_DATE")
                            );

        $this->cmd = VARS::request("cmd");

        $this->selectdata   = VARS::post("selectdata");
        $this->checked      = VARS::post("CHECKED");

        //list→edit
        if (VARS::request("SCHDIV") != "") {
            $this->schdiv = VARS::request("SCHDIV");
        }
        if (VARS::request("SCHOOL_KIND") != "") {
            $this->schoolkind = VARS::request("SCHOOL_KIND");
        }
        if (VARS::request("COLLECT_GRP_CD") != "") {
            $this->collect_grp_cd = VARS::request("COLLECT_GRP_CD");
        }

        //絞り込み条件
        $this->grade_cm         = VARS::request("GRADE_CM");
        $this->coursecode       = VARS::request("COURSECODE");
        $this->grade_hr_class   = VARS::request("GRADE_HR_CLASS");
        $this->clubcd           = VARS::request("CLUBCD");
        $this->domi_cd          = VARS::request("DOMI_CD");

        //新入生の時、年度はログイン年度 + 1
        $this->year = ($this->schdiv == "1") ? CTRL_YEAR + 1: CTRL_YEAR;
    }

    //入力項目チェック
    function validateInput($cmd) {
        //更新
        if ($cmd == "update") {
            //必須項目チェック
            if (!$this->field["COLLECT_PATTERN_CD"]) {
                $this->setWarning("MSG310", "( 入金パターン )");
                return false;
            }
            if (!$this->field["SLIP_DATE"]) {
                $this->setWarning("MSG301", "( 伝票日付 )");
                return false;
            }
            if (!strlen($this->selectdata)) {
                $this->setWarning("MSG304", "( 対象者 )");
                return false;
            }
            if ($this->schoolkind == "" || $this->collect_grp_cd == "") {
                $this->setWarning("MSG304");
                return false;
            }

            //必須項目が変更されていないか
            if ($this->field["SCHDIV"] != $this->schdiv) {
                $this->setWarning("MSG308","( 新入生／在校生 )");
                return false;
            }
            if ($this->field["SCHOOL_KIND"] != $this->schoolkind) {
                $this->setWarning("MSG308","( 校種 )");
                return false;
            }
            if ($this->field["COLLECT_GRP_CD"] != $this->collect_grp_cd) {
                $this->setWarning("MSG308","( 入金グループ )");
                return false;
            }

            //入金グループデータと入金項目別入金計画データが不一致の時
            $db = Query::dbCheckOut();
            $grpDat      = $db->getOne(knjp712Query::getCollectGrpDatMonthDat($this, "COLLECT_GRP_DAT"));
            $monthGrpDat = $db->getOne(knjp712Query::getCollectGrpDatMonthDat($this, "COLLECT_MONTH_GRP_DAT"));
            Query::dbCheckIn($db);
            if ($grpDat != $monthGrpDat) {
                $this->setWarning("MSG305","( 入金項目別入金計画データ )");
                return false;
            }
        }

        //削除
        if ($cmd == "delete") {
            //必須項目チェック
            if (!get_count($this->checked)) {
                $this->setWarning("チェックボックスを選択してください");
                return false;
            }
            if ($this->schdiv == "" || $this->schoolkind == "" || $this->collect_grp_cd == "") {
                $this->setWarning("MSG304");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            if ($this->update()) {
                $this->setMessage("MSG201");
                return;
            }
        }
    }
    function &update() {
        $result = knjp712Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    //削除
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            if ($this->delete()) {
                $this->setMessage("MSG201");
                return;
            }
        }
    }
    function &delete() {
        $result = knjp712Query::getDeleteQuery($this);
        return $result;
    }
}
?>
