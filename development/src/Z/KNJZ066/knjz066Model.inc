<?php

require_once('for_php7.php');

class knjz066Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjz066Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->school_kind);
            unset($this->grade);
            unset($this->condition);
            unset($this->levelcnt);
            $this->field = array();
        }

        if (VARS::post("SCHOOL_KIND") != "") {
            $this->school_kind = VARS::post("SCHOOL_KIND");
        }
        if (VARS::post("GRADE") != ""){
            $this->grade = VARS::post("GRADE");
        }
        if (VARS::post("CONDITION") != ""){
            $this->condition = VARS::post("CONDITION");
        }
        if (VARS::post("LEVELCNT") != ""){
            $this->levelcnt = VARS::post("LEVELCNT");
        }

        if ($this->levelcnt > 0) {
            $this->field = array();
            for ($i = 1; $i <= $this->levelcnt; $i++) {
                $this->field["ASSESSMARK_".$i] =  VARS::post("ASSESSMARK_".$i);
            }
        } else {
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {

        if ($this->cmd == "update") {
            //必須チェック
            if ($this->school_kind == "") {
                $this->setWarning("MSG304", "　　　（ 学部 ）");
                return false;
            }
            if ($this->grade == "") {
                $this->setWarning("MSG304", "　　　（ 学年 ）");
                return false;
            }
            if ($this->condition == "") {
                $this->setWarning("MSG304", "　　（ 状態区分 ）");
                return false;
            }
            if ($this->levelcnt == "") {
                $this->setWarning("MSG304", "　　　（ 段階数 ）");
                return false;
            }

            //テキストエリア入力チェック
            for ($i = 1; $i <= $this->levelcnt; $i++) {
                if (strlen($this->field["ASSESSMARK_".$i]) > 6 ) {
                    $this->setWarning("MSG914","(評定記号は6バイトまでです)");
                    return false;
                }
            }
        } else if ($this->cmd == "delete") {
            //必須チェック
            if ($this->school_kind == "") {
                $this->setWarning("MSG304", "　　　（ 学部 ）");
                return false;
            }
            if ($this->grade == "") {
                $this->setWarning("MSG304", "　　　（ 学年 ）");
                return false;
            }
            if ($this->condition == "") {
                $this->setWarning("MSG304", "　　（ 状態区分 ）");
                return false;
            }
        } else if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $cnt_pre = $db->getOne(knjz066Query::chkCopyData(CTRL_YEAR-1, "check"));
            $cnt_this = $db->getOne(knjz066Query::chkCopyData(CTRL_YEAR, "check"));
            Query::dbCheckIn($db);

            //存在チェック（前年度）
            if ($cnt_pre == 0) {
                $this->setWarning("MSG917","前年度のデータが存在しません。");
                return false;
            }

            //存在チェック（今年度）
            if ($cnt_this > 0) {
                $this->setWarning("MSG917","今年度にデータが存在します。");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz066Query::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->setCmd("main");
        } else {
            $this->setCmd("check");
        }
        return;
    }

    //削除
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz066Query::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $result = $db->query(knjz066Query::chkCopyData(CTRL_YEAR-1, "list"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjz066Query::getCopyQuery($row));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
