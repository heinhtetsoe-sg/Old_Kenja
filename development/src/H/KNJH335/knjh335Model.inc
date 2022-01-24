<?php

require_once('for_php7.php');

class knjh335Model extends Model {
    var $field = array();
    var $cmd;
    var $programid;
    var $groupcd;
    var $resetGroup;
    var $group_div;
    var $stf_auth_cd;
    var $resetStf_cd;

    function knjh335Model()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->programid = VARS::get("PROGRAMID");
            $this->group_div = ($this->programid == "KNJH334") ? "2" : "1";
            $this->clean();
        }

        $this->cmd = VARS::request("cmd");
        $this->group_div   = (VARS::request("GROUP_DIV")) ? VARS::request("GROUP_DIV") : $this->group_div;
        $this->groupcd     = (VARS::request("GROUPCD")) ? sprintf("%04d", VARS::request("GROUPCD")) : $this->groupcd;
        $this->stf_auth_cd = (VARS::request("STF_AUTH_CD")) ? VARS::request("STF_AUTH_CD") : $this->stf_auth_cd;

        $this->resetGroup  = (VARS::get("GROUPCD")) ? VARS::get("GROUPCD") : $this->resetGroup;
        $this->resetStf_cd = (VARS::get("STF_AUTH_CD")) ? VARS::get("STF_AUTH_CD") : $this->resetStf_cd;

        //取消押下
        if ($this->cmd == "reset") {
            $this->groupcd     = $this->resetGroup;
            $this->stf_auth_cd = $this->resetStf_cd;
        }

        $this->field = array("GROUP_DIV"    => $this->group_div,
                             "GROUPCD"      => sprintf("%04d", VARS::post("GROUPCD")),
                             "STF_AUTH_CD"  => VARS::post("STF_AUTH_CD"),
                             "GROUPNAME1"   => VARS::post("GROUPNAME1"),
                             "GROUPNAME2"   => VARS::post("GROUPNAME2"),
                             "GROUPNAME3"   => VARS::post("GROUPNAME3")
                             );

        if ($this->cmd == "divChange") {
            $this->clean();
        }
    }

    // 変数をクリア
    function clean()
    {
        unset($this->groupcd);
        unset($this->resetGroup);
        unset($this->stf_auth_cd);
        unset($this->resetStf_cd);
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (!$this->keyCheck()) {
            return false;
        }

        //キー値変更
        if ($this->cmd != "add") {
            if (!$this->keyChange()) {
                return false;
            }
        }

        //存在チェック
        if (!$this->exisCheck()) {
            return false;
        }

        //文字数チェック
        if ($this->cmd != "delete") {
            if (!$this->strLenCheck()) {
                return false;
            }
        }

        return true;
    }

    //キーのチェック
    function keyCheck()
    {
        $checkStrMes = array("GROUP_DIV" => "職員/権限", "GROUPCD" => "グループコード", "STF_AUTH_CD" => "ユーザーコード");
        foreach ($checkStrMes as $key => $val) {
            if ($this->field[$key] == "") {
                $this->setWarning("MSG301","(".$val.")");
                return false;
            }
        }
        return true;
    }

    //キー値変更チェック
    function keyChange()
    {
        if ($this->resetGroup != $this->field["GROUPCD"] OR
            $this->resetStf_cd != $this->field["STF_AUTH_CD"])
        {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        return true;
    }

    //存在チェック
    function exisCheck()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $row = $db->getRow(knjh335Query::getRow($this, "CHECK"), DB_FETCHMODE_ASSOC);
        if ($this->cmd != "add" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }

        $cnt = $db->getOne(knjh335Query::getGroupData($this->field));
        //データ登録チェック
        if ($this->cmd == "delete" && $cnt > 0) {
            $this->setWarning("MSG919", "グループデータが登録されています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //文字数チェック
    function strLenCheck()
    {
        $checkStrLen = array("GROUPNAME1" => 60,
                             "GROUPNAME2" => 60,
                             "GROUPNAME3" => 60);

        $checkStrMes = array("GROUPNAME1" => "グループ名称１",
                             "GROUPNAME2" => "グループ名称２",
                             "GROUPNAME3" => "グループ名称３");

        foreach ($checkStrLen as $key => $val) {
            if (strlen(trim($this->field["GROUPNAME1"])) == 0) {
                $this->setWarning("MSG301","(".$checkStrMes[$key].")");
                return false;
            } else if (strlen($this->field[$key]) > $val ) {
                $this->setWarning("MSG915", "(".$checkStrMes[$key].") ".$val."バイトまで");
                return false;
            }
        }
        return true;
    }

    //追加
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjh335Query::getInsertQuery($this->field);
            $this->resetGroup  = $this->field["GROUPCD"];
            $this->resetStf_cd = $this->field["STF_AUTH_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjh335Query::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
    }

    //削除
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjh335Query::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->clean();
        }
    }
}
?>
