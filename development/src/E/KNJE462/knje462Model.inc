<?php

require_once('for_php7.php');

class knje462Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knje462Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "" || VARS::request("cmd") == "clear") {
            unset($this->compcnt);
            $this->field = array();
        }

        if (VARS::request("cmd") != "clear") {
            if (VARS::post("COMPCNT") != ""){
                $this->compcnt = VARS::post("COMPCNT");
            }
            if ($this->compcnt > 0) {
                $this->field = array();
                for ($i = 1; $i <= $this->compcnt; $i++) {
                    $this->field["BASETITLENAME_INFLG_".$i] =  VARS::post("BASETITLENAME_INFLG_".$i);
                    $this->field["BASETITLENAME_".$i] =  VARS::post("BASETITLENAME_".$i);
                }
            } else {
                $this->field = array();
            }
        } else {
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {

        if ($this->cmd == "update") {
            //必須チェック
            if ($this->compcnt == "") {
                $this->setWarning("MSG304", "　　　（ 項目数 ）");
                return false;
            }

            //テキストエリア入力チェック
            $errMsgArr = array();
            for ($i = 1; $i <= $this->compcnt; $i++) {
                if (!strlen(trim($this->field["BASETITLENAME_".$i]))) {
                    $errMsgArr[] = "項目".$i;
                }
                if (strlen($this->field["BASETITLENAME_".$i]) > 100 ) {
                    $this->setWarning("MSG914","(登録文字列は100バイトまでです)");
                    return false;
                }
            }
            if ($errMsgArr) {
                $this->setWarning("MSG301","(".implode(",", $errMsgArr).")");
                return false;
            }
        } else if ($this->cmd == "delete") {
            //必須チェック
            $db = Query::dbCheckOut();
            $cnt = $db->getOne(knje462Query::getCntChallengedSupportBaseInfoYmst(CTRL_YEAR));
            Query::dbCheckIn($db);
            if ($cnt == 0) {
                $this->setWarning("MSG303", "　　　（ 登録データなし ）");
                return false;
            }
        } else if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $cnt_preHdat    = $db->getOne(knje462Query::getCntChallengedSupportBaseInfoYmst(CTRL_YEAR-1));
            $cnt_thisHdat   = $db->getOne(knje462Query::getCntChallengedSupportBaseInfoYmst(CTRL_YEAR));
            Query::dbCheckIn($db);

            //存在チェック（前年度）
            if ($cnt_preHdat == 0) {
                $this->setWarning("MSG917","前年度のデータが存在しません。");
                return false;
            }

            //存在チェック（今年度）
            if ($cnt_thisHdat > 0) {
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
            knje462Query::getInsertQuery($this);
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
            $db = Query::dbCheckOut();
            knje462Query::getDeleteQuery($this, $db);
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $result = $db->query(knje462Query::chkCopyData(CTRL_YEAR-1, "list", "CHALLENGED_SUPPORT_BASE_INFO_YMST"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knje462Query::getCopyQuery($row, "CHALLENGED_SUPPORT_BASE_INFO_YMST"));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
