<?php

require_once('for_php7.php');

class knjl425Model extends Model {
    var $field = array();
    var $cmd;

    function knjl425Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->leftSendCd);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("LEFT_EVENT_CLASS_CD")) {
            $this->leftSendCd = VARS::get("LEFT_EVENT_CLASS_CD");
        }

        $this->field = array("EVENT_CLASS_CD"      =>  (VARS::post("EVENT_CLASS_CD")) ? sprintf("%03d",VARS::post("EVENT_CLASS_CD")) : "",
                             "EVENT_CLASS_NAME"    =>  VARS::post("EVENT_CLASS_NAME")
                             );

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (trim($this->field["EVENT_CLASS_CD"]) == "") {
            $this->setWarning("MSG301","(分類コード)");
            return false;
        }
        if (trim($this->field["EVENT_CLASS_NAME"]) == "") {
            $this->setWarning("MSG301","(分類名称)");
            return false;
        }

        if ($this->cmd == "update" && $this->leftSendCd != $this->field["EVENT_CLASS_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //１レコード取得
        $db = Query::dbCheckOut(); //DB接続
        $query = knjl425Query::getRecruitSendYmst($this, "CHECK");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(分類コード)");
            return false;
        }
        //文字数チェック
        $checkStrLen["EVENT_CLASS_NAME"] = array("LEN" => 120, "MESAGE" => "(分類名称)");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
        return true;
    }
    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->leftSendCd = $this->field["EVENT_CLASS_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加時の内部メソッド
    function &insert() {
        knjl425Query::getInsertQuery($this->field, $this);
        return;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新時の内部メソッド
    function &update() {
        knjl425Query::getUpdateQuery($this->field, $this);
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->leftSendCd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->leftSendCd != $this->field["EVENT_CLASS_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //存在チェック
        $db = Query::dbCheckOut(); //DB接続
        $query = knjl425Query::getRecruitSendYmst($this, "CHECK");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }

        //削除
        knjl425Query::getDeleteQuery($this->field, $this);
        $this->leftSendCd = "";
        $this->field = array();
        $this->setMessage("MSG201");
        return;
    }
}
?>
