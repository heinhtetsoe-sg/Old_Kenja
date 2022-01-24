<?php

require_once('for_php7.php');

class knjl422Model extends Model {
    var $field = array();
    var $cmd;

    function knjl422Model()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->leftEventCd);
            unset($this->leftEventClassCd);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::post("LEFT_YEAR")) {
            $this->leftYear = VARS::post("LEFT_YEAR");
        }
        if (VARS::post("LEFT_EVENT_CLASS_CD")) {
            $this->leftEventClassCd = VARS::post("LEFT_EVENT_CLASS_CD");
        }

        if (VARS::get("LEFT_YEAR")) {
            $this->leftYear = VARS::get("LEFT_YEAR");
        }
        if (VARS::get("LEFT_EVENT_CLASS_CD")) {
            $this->leftEventClassCd = VARS::get("LEFT_EVENT_CLASS_CD");
        }
        if (VARS::get("LEFT_EVENT_CD")) {
            $this->leftEventCd = VARS::get("LEFT_EVENT_CD");
        }

        $this->field = array("EVENT_CD"     =>  (VARS::post("EVENT_CD")) ? sprintf("%03d",VARS::post("EVENT_CD")) : "",
                             "EVENT_NAME"   =>  VARS::post("EVENT_NAME"),
                             "EVENT_ABBV"   =>  VARS::post("EVENT_ABBV"),
                             "REMARK"       =>  VARS::post("REMARK")
                             );

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (trim($this->field["EVENT_CD"]) == "") {
            $this->setWarning("MSG301","(イベントコード)");
            return false;
        }
        if (trim($this->leftEventClassCd) == "") {
            $this->setWarning("MSG301","(分類コード)");
            return false;
        }
        if (trim($this->field["EVENT_NAME"]) == "") {
            $this->setWarning("MSG301","(イベント名称)");
            return false;
        }

        if ($this->cmd == "update" && $this->leftEventCd != $this->field["EVENT_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //１レコード取得
        $db = Query::dbCheckOut(); //DB接続
        $query = knjl422Query::getRecruitEventYmst($this, "CHECK");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(イベントコード)");
            return false;
        }
        //文字数チェック
        $checkStrLen["EVENT_NAME"] = array("LEN" => 120, "MESAGE" => "(イベント名称)");
        $checkStrLen["EVENT_ABBV"] = array("LEN" =>  60, "MESAGE" => "(イベント略称)");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
        //行数チェック
        if ($this->validate_row_cnt($this->field["REMARK"], (40 * 2)) > 2) {
            $this->setWarning("MSG915", "(イベント備考) 2行まで");
            return false;
        }

        return true;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->leftEventCd = $this->field["EVENT_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加時の内部メソッド
    function &insert() {
        knjl422Query::getInsertQuery($this->field, $this);
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
        knjl422Query::getUpdateQuery($this->field, $this);
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->leftEventCd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->leftEventCd != $this->field["EVENT_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //存在チェック
        $db = Query::dbCheckOut(); //DB接続
        $query = knjl422Query::getRecruitEventYmst($this, "CHECK");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }

        //削除
        knjl422Query::getDeleteQuery($this->field, $this);
        $this->leftEventCd = "";
        $this->field = array();
        $this->setMessage("MSG201");
        return;
   }

    //コピー用のモデルを返します
    function &getCopyModel() {
        knjl422Query::getCopyQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
