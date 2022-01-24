<?php

require_once('for_php7.php');

class knjh331Model extends Model {
    var $field      = array();
    var $cmd;
    var $targetcd;        //primary
    var $mockdiv;         //primary
    var $staffcd;
    var $staffname;
    var $target_div;
    var $resetTarget;
    var $resetMockdiv;
    var $resetStf_cd;

    function knjh331Model()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->target_div = "1";
            $this->clean();
        }
        $this->cmd          = VARS::request("cmd");

        $this->target_div  = (VARS::request("TARGET_DIV")) ? VARS::request("TARGET_DIV") : $this->target_div;
        $this->targetcd    = (VARS::get("TARGETCD")) ? sprintf("%08d", substr(VARS::get("TARGETCD"), 1)) : $this->targetcd;
        $this->mockdiv     = (VARS::get("TARGETCD")) ? substr(VARS::get("TARGETCD"), 0, 1) : $this->mockdiv;
        $this->stf_auth_cd = (VARS::request("STF_AUTH_CD")) ? VARS::request("STF_AUTH_CD") : $this->stf_auth_cd;

        $this->resetTarget  = (VARS::get("TARGETCD")) ? sprintf("%08d", substr(VARS::get("TARGETCD"), 1)) : $this->resetTarget;
        $this->resetMockdiv = (VARS::get("TARGETCD")) ? substr(VARS::get("TARGETCD"), 0, 1) : $this->resetMockdiv;
        $this->resetStf_cd  = (VARS::get("STF_AUTH_CD")) ? VARS::get("STF_AUTH_CD") : $this->resetStf_cd;

        $this->field = array("TARGET_DIV"   => $this->target_div,
                             "MOCK_DIV"     => VARS::post("MOCK_DIV"),
                             "TARGETCD"     => sprintf("%08d", VARS::post("TARGETCD")),
                             "STF_AUTH_CD"  => VARS::post("STF_AUTH_CD"),
                             "TARGETNAME1"  => VARS::post("TARGETNAME1"),
                             "TARGETNAME2"  => VARS::post("TARGETNAME2"),
                             "TARGETNAME3"  => VARS::post("TARGETNAME3")
                             );
        //取消押下
        if ($this->cmd == "reset") {
            $this->mockdiv     = $this->resetMockdiv;
            $this->targetcd    = $this->resetTarget;
            $this->stf_auth_cd = $this->resetStf_cd;
        }

        if ($this->cmd == "divChange") {
            $this->clean();
        }
    }

    // 変数をクリア
    function clean()
    {
        unset($this->mockdiv);
        unset($this->resetMockdiv);
        unset($this->targetcd);
        unset($this->resetTarget);
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
        $checkStrMes = array("TARGET_DIV" => "職員/グループ", "MOCK_DIV" => "目標値コード", "TARGETCD" => "目標値コード", "STF_AUTH_CD" => "ユーザーコード");
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
        if ($this->resetMockdiv != $this->field["MOCK_DIV"] OR
            $this->resetTarget != $this->field["TARGETCD"] OR
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

        $row = $db->getRow(knjh331Query::getRow($this, "CHECK"), DB_FETCHMODE_ASSOC);
        if ($this->cmd != "add" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }

        $cnt = $db->getOne(knjh331Query::getGroupData($this->field));
        //データ登録チェック
        if ($this->cmd == "delete" && $cnt > 0) {
            $this->setWarning("MSG919", "目標値データが登録されています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //文字数チェック
    function strLenCheck()
    {
        $checkStrLen = array("TARGETNAME1" => 60,
                             "TARGETNAME2" => 60,
                             "TARGETNAME3" => 60);

        $checkStrMes = array("TARGETNAME1" => "目標値名称",
                             "TARGETNAME2" => "目標値略称１",
                             "TARGETNAME3" => "目標値略称２");

        foreach ($checkStrLen as $key => $val) {
            if (strlen(trim($this->field["TARGETNAME1"])) == 0) {
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
        if ($this->validateInput()){
            knjh331Query::getInsertQuery($this->field);
            $this->mockdiv  = $this->field["MOCK_DIV"];
            $this->targetcd = $this->field["TARGETCD"];
            $this->resetMockdiv = $this->field["MOCK_DIV"];
            $this->resetTarget  = $this->field["TARGETCD"];
            $this->resetStf_cd  = $this->field["STF_AUTH_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjh331Query::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }    
    }

    //削除
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjh331Query::getDeleteQuery($this->field);
            $this->clean();
            $this->setMessage("MSG201");
        }
    }
}
?>
