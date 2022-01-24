<?php

require_once('for_php7.php');

class knjz064aModel extends Model {
    var $cmd;
    var $ctrl;
    var $year;
    var $semester;
    var $gakubu_school_kind = "";
    var $hr_class = "";
    var $hr_class2 = "";
    var $condition = "";
    var $groupcd = "";
    var $field;
    var $selectdata = "";
    var $selectdata2 = "";

    function knjz064aModel() {
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
            unset($this->hr_class);
            unset($this->hr_class2);
            unset($this->condition);
            unset($this->groupcd);
            $this->field = array();
        }
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if (VARS::post("SEMESTER") != "") {
            $this->semester = VARS::request("SEMESTER");
        }
        if (VARS::post("GAKUBU_SCHOOL_KIND") != "") {
            $this->gakubu_school_kind = VARS::request("GAKUBU_SCHOOL_KIND");
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

        $this->field = array("CONDITION"            =>  VARS::post("CONDITION"),        //状態区分
                             "GROUPCD"              =>  (VARS::post("GROUPCD") == "") ? "" : sprintf("%04d", VARS::post("GROUPCD")),     //グループコード
                             "GROUPNAME"            =>  VARS::post("GROUPNAME"),        //グループ名
                             "GROUPABBV"            =>  VARS::post("GROUPABBV"),        //グループ名略称
                             "GUIDANCE_PATTERN"     =>  VARS::post("GUIDANCE_PATTERN"), //指導計画帳票パターン
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->selectdata2 = VARS::post("selectdata2");

        $this->cmd = VARS::request("cmd");

        //初期値設定
        $db = Query::dbCheckOut();
        //学部
        $gakubu_school_kind = $db->getOne(knjz064aQuery::getSchoolKind());
        $this->gakubu_school_kind = ($this->gakubu_school_kind) ? $this->gakubu_school_kind : $gakubu_school_kind;
        //年組
        $hr_class = $db->getOne(knjz064aQuery::getHrClass($this));
        $this->hr_class = ($this->hr_class != "") ? $this->hr_class : $hr_class;
        //年組（特別クラス選択時）
        $hr_class2 = $db->getOne(knjz064aQuery::getHrClass2($this));
        $this->hr_class2 = ($this->hr_class2 != "") ? $this->hr_class2 : (($this->hr_class2 == "") ? '00-000' : $hr_class2);
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
    
        $db = Query::dbCheckOut();
        if ($this->cmd == "update") {
            //必須入力チェック
            if (!$this->gakubu_school_kind || $this->hr_class == "" || $this->hr_class2 == "" || !$this->field["CONDITION"] || !$this->field["GROUPCD"] || !$this->field["GROUPNAME"]) {
                $this->setWarning("MSG304");
                $this->groupcd = "";
                return false;
            }
            //文字数チェック
            if (strlen($this->field["GROUPCD"]) > 4 ) {
                $this->setWarning("MSG915", "(グループコード) 4バイトまで");
                return false;
            } else if (strlen($this->field["GROUPNAME"]) > 90 ) {
                $this->setWarning("MSG915", "(名称) 90バイトまで");
                return false;
            } else if (strlen($this->field["GROUPABBV"]) > 90 ) {
                $this->setWarning("MSG915", "(略称) 90バイトまで");
                return false;
            }
            //更新チェック
            if ($this->groupcd != $this->field["GROUPCD"] || $this->condition != $this->field["CONDITION"]) {
                $getData = $db->getOne(knjz064aQuery::getList($this, $this->field["CONDITION"], $this->field["GROUPCD"]));
                if ($getData > 0) {
                    $this->setWarning("MSG308");
                    $this->groupcd = "";
                    return false;
                }
            }
        } else if ($this->cmd == "copy") {
            //存在チェック（前年度）
            $cnt_preYM = $db->getOne(knjz064aQuery::checkCompGroupYMst(intval($this->year - 1)));
            $cnt_preD  = $db->getOne(knjz064aQuery::checkCompGroupDat(intval($this->year - 1)));
            if ($cnt_preYM == 0 || $cnt_preD == 0) {
                $this->setWarning("MSG917","対象データが存在しません。");
                return false;
            }

            //存在チェック（指定年度）
            $cnt_thisYM = $db->getOne(knjz064aQuery::checkCompGroupYMst($this->year));
            $cnt_thisD  = $db->getOne(knjz064aQuery::checkCompGroupDat($this->year));
            if ($cnt_thisYM > 0 || $cnt_thisD > 0) {
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
            knjz064aQuery::UpdateQuery($this);
            $this->getUpdateModel2();
            //更新時にグループコードをセット
            if ($this->selectdata) {
                $this->condition = $this->field["CONDITION"];
                $this->groupcd = $this->field["GROUPCD"];
            } else {
                $this->field = array();
            }
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理狙い用
    function &getUpdateModel2() {
        knjz064aQuery::UpdateQueryAim($this);
        //更新時にグループコードをセット
        if ($this->selectdata2) {
            $this->condition = $this->field["CONDITION"];
            $this->groupcd = $this->field["GROUPCD"];
        }
        $this->setMessage("MSG201");
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $key = "";
            //前年度のデータ取得
            $result = $db->query(knjz064aQuery::copyCompGroupDat($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($key !== $row["SEMESTER"].'-'.$row["GAKUBU_SCHOOL_KIND"].'-'.$row["CONDITION"].'-'.$row["GROUPCD"]) {
                    //コピー    -- GRADE_KIND_COMP_GROUP_YMST
                    $db->query(knjz064aQuery::copyInsertQueryYMst($this, $row));
                }
                //コピー    -- GRADE_KIND_COMP_GROUP_DAT
                $db->query(knjz064aQuery::copyInsertQueryDat($this, $row));

                $key = $row["SEMESTER"].'-'.$row["GAKUBU_SCHOOL_KIND"].'-'.$row["CONDITION"].'-'.$row["GROUPCD"];
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
