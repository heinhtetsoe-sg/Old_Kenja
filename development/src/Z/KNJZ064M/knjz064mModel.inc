<?php

require_once('for_php7.php');
class knjz064mModel extends Model {
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
    var $field;
    var $selectdata = "";

    function knjz064mModel() {
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

        $this->field = array("CONDITION"            =>  VARS::post("CONDITION"),        //状態区分
                             "GROUPCD"              =>  (VARS::post("GROUPCD") == "") ? "" : sprintf("%04d", VARS::post("GROUPCD")),     //グループコード
                             "GROUPNAME"            =>  VARS::post("GROUPNAME"),        //グループ名
                            );

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");

    }

    //入力項目チェック
    function validateInput() {
    
        $db = Query::dbCheckOut();
        if ($this->cmd == "insert" || $this->cmd == "update" || $this->cmd == "delete") {
            //文字数チェック
            if (strlen($this->field["GROUPCD"]) > 4 ) {
                $this->setWarning("MSG915", "(グループコード) 4バイトまで");
                return false;
            } else if (strlen($this->field["GROUPNAME"]) > 60 ) {
                $this->setWarning("MSG915", "(科目グループ名) 全角20文字まで");
                return false;
            }
            //更新チェック
            if ($this->cmd == "update" || $this->cmd == "delete") {
                if ($this->groupcd != $this->field["GROUPCD"] || $this->condition != $this->field["CONDITION"]) {
                    $this->setWarning("MSG304");
                    return false;
                }
            }
            //登録チェック
            if ($this->cmd == "insert") {
                $cnt = $db->getOne(knjz064mQuery::getList($this, $this->field["CONDITION"], $this->field["GROUPCD"]));
                if ($cnt > 0) {
                    $this->setWarning("MSG302");
                    return false;
                }
            }

            //削除チェック
            if ($this->cmd == "delete") {
                //単元設定取得
                $query = knjz064mQuery::selectGradeKindUnitGroupYMst($this);
                $unitRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                //生徒登録取得
                $query = knjz064mQuery::selectGradeKindSchregGroupDat($this);
                $schregRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($unitRow && $schregRow) {
                    $msg  = "科目グループに単元・生徒が登録されています。\\n";
                    $msg .= "個別の指導計画（単元設定）からデータを削除してください。\\n";
                    $msg .= "個別の指導計画（生徒登録）からデータを削除してください。\\n";
                    $this->setWarning("MSG919", $msg);
                    return false;
                } else if ($unitRow) {
                    $msg  = "科目グループに単元が登録されています。\\n";
                    $msg .= "個別の指導計画（単元設定）からデータを削除してください。\\n";
                    $this->setWarning("MSG919", $msg);
                    return false;
                } else if ($schregRow) {
                    $msg  = "科目グループに生徒が登録されています。\\n";
                    $msg .= "個別の指導計画（生徒登録）からデータを削除してください。\\n";
                    $this->setWarning("MSG919", $msg);
                    return false;
                }

            }

        } else if ($this->cmd == "copy") {
            //存在チェック（前年度）
            $cnt_preYM = $db->getOne(knjz064mQuery::checkCompGroupYMst($this, intval($this->year - 1)));
            $cnt_preD  = $db->getOne(knjz064mQuery::checkCompGroupDat($this, intval($this->year - 1)));
            if ($cnt_preYM == 0 || $cnt_preD == 0) {
                $this->setWarning("MSG917","対象データが存在しません。");
                return false;
            }

            //存在チェック（指定年度）
            $cnt_thisYM = $db->getOne(knjz064mQuery::checkCompGroupYMst($this, $this->year));
            $cnt_thisD  = $db->getOne(knjz064mQuery::checkCompGroupDat($this, $this->year));
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
            knjz064mQuery::UpdateQuery($this);
            //更新時にグループコードをセット
            if ($this->cmd != "delete") {
                $this->condition = $this->field["CONDITION"];
                $this->groupcd = $this->field["GROUPCD"];
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
            $key = "";
            //前年度のデータ取得
            $query = knjz064mQuery::copyCompGroupDat($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($key !== $row["SEMESTER"].'-'.$row["GAKUBU_SCHOOL_KIND"].'-'.$row["CONDITION"].'-'.$row["GROUPCD"]) {
                    //コピー    -- GRADE_KIND_COMP_GROUP_YMST
                    $db->query(knjz064mQuery::copyInsertQueryYMst($this, $row));
                }
                //コピー    -- GRADE_KIND_COMP_GROUP_DAT
                $db->query(knjz064mQuery::copyInsertQueryDat($this, $row));

                $key = $row["SEMESTER"].'-'.$row["GAKUBU_SCHOOL_KIND"].'-'.$row["CONDITION"].'-'.$row["GROUPCD"];
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz064mQuery::UpdateQuery($this);
            //更新時にグループコードをセット
            if ($this->selectdata) {
                $this->condition = "";
                $this->groupcd = "";
            } else {
                $this->field = array();
            }
            $this->setMessage("MSG201");
        }
        return;
    }

}
?>
