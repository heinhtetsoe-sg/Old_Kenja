<?php

require_once('for_php7.php');

class knjz064Model extends Model {
    var $cmd;
    var $ctrl;
    var $school_kind = "";
    var $condition = "";
    var $selectdata = "";

    function knjz064Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->school_kind);
            unset($this->condition);
        }

        if (VARS::post("SCHOOL_KIND") != "") {
            $this->school_kind = VARS::post("SCHOOL_KIND");
        }
        if (VARS::post("CONDITION") != ""){
            $this->condition = VARS::post("CONDITION");
        }

        if (VARS::request("CONDITION") != "") {
            $this->condition = VARS::request("CONDITION");
        }

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");

        //初期値設定（学部）
        $db = Query::dbCheckOut();
        $school_kind = $db->getOne(knjz064Query::getSchoolKind());
        Query::dbCheckIn($db);
        $this->school_kind = ($this->school_kind) ? $this->school_kind : $school_kind;
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "update") {
            //必須入力チェック
            if (!$this->school_kind || !$this->condition) {
                $this->setWarning("MSG304");
                return false;
            }
        } else if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            //存在チェック（前年度）
            $cnt_pre = $db->getOne(knjz064Query::checkGradeKindCompSubclassDat(CTRL_YEAR-1));
            if ($cnt_pre == 0) {
                $this->setWarning("MSG917","前年度のデータが存在しません。");
                return false;
            }

            //存在チェック（今年度）
            $cnt_this = $db->getOne(knjz064Query::checkGradeKindCompSubclassDat(CTRL_YEAR));
            if ($cnt_this > 0) {
                $this->setWarning("MSG917","今年度にデータが存在します。");
                return false;
            }
            Query::dbCheckIn($db);
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz064Query::UpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $result = $db->query(knjz064Query::copyGradeKindCompSubclassDat());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjz064Query::copyInsertQuery($row));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
