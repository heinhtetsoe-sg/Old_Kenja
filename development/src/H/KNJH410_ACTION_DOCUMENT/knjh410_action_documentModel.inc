<?php

class knjh410_action_documentModel extends Model {

    var $cmd;
    var $cmdSub;
    var $schregno;
    var $delchk;
    var $dividecd;
    var $narrowing;
    var $taitleSort = array();
    var $sort;
    var $staffsort;
    var $datesort;
    var $titlesort;
    var $control_data = array();    //コントロールマスタデータを保持
    
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjh410_action_documentModel() {
        common::GetControlMaster_Fnc($this->control_data);
    }
 
    function init()
    {
        //DB接続
        $db = Query::dbCheckOut();

        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            unset($this->sendAuth);
            unset($this->getPrgId);
            unset($this->schregno);
            unset($this->actiondate);
            unset($this->seq);
            unset($this->sendCmd);
            $this->sendCmd  = VARS::get("cmd");
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        if(VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }

        $this->dividecd = VARS::post("DIVIDECD");

        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd      = VARS::request("cmd");
        if (VARS::post("sendCmd")) {
            $this->sendCmd = VARS::post("sendCmd");
        }
        
        $this->mode     = VARS::request("mode") != "" ? VARS::request("mode") : $this->mode;
        if($this->cmd == ""){
            $this->mode = "ungrd";
        }
        if($this->cmd == ""){
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        }
        if(VARS::request("SCHREGNO") != "" && $this->cmd != "radio"){
            $this->year = VARS::request("GRD_YEAR") != "" ? VARS::request("GRD_YEAR") : CTRL_YEAR;
            $this->semester = VARS::request("GRD_SEMESTER") != "" ? VARS::request("GRD_SEMESTER") : CTRL_SEMESTER;
        }
        if ($this->cmd == "updateSub") {
            $this->actiondate    = str_replace("-", "/", VARS::get("ACTIONDATE"));
            $this->actiondateUpd = str_replace("-", "/", VARS::get("ACTIONDATE"));
            $this->seq = VARS::get("SEQ");
        } elseif ($this->cmd == "update" || $this->cmd == "insert") {
            $this->actiondate = VARS::post("ACTIONDATE");
            $this->actiontime = (VARS::post("ACTIONHOUR")) ? sprintf("%02d",VARS::post("ACTIONHOUR")).":".sprintf("%02d",VARS::post("ACTIONMINUTE")).":00" : "";
            $this->title      = VARS::post("TITLE");
            $this->text       = VARS::post("TEXT");
            $this->private    = VARS::post("PRIVATE");
            if ($this->cmd == "insert") {
                $maxseq = array();
                $maxseq = $db->getRow(knjh410_action_documentQuery::getMaxSeq($this->schregno), DB_FETCHMODE_ASSOC);
                $this->seq = ($maxseq["SEQ"]) ? $maxseq["SEQ"] : 1;
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }


    //更新処理
    function &getUpdateModel()
    {
        if (!$this->dataCheck()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    //データチェック
    function dataCheck()
    {
        if (strlen($this->title) > 120) {
            $this->setWarning("MSG915","件名は120バイトまでです。");
            return false;
        }
        if (strlen($this->text) > 700) {
            $this->setWarning("MSG915","内容は700バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function update()
    {
        if ($this->cmd == "insert") {
            knjh410_action_documentQuery::insertQuery($this);
        } else {
            knjh410_action_documentQuery::updateQuery($this);
        }
    }

    //編集完了用のモデルを返します
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        for ($i = 0; $i < count($this->delchk); $i++) {
            $delvalue = split(":", $this->delchk[$i]);
            $query = knjh410_action_documentQuery::getDeleteQuery($delvalue);

            $db->query($query);
        }
        $this->setMessage("MSG201");
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
}
?>
