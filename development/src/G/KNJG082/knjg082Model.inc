<?php

require_once('for_php7.php');

class knjg082Model extends Model {
    var $cmd;
    var $field      = array();
    var $sendField  = array();
    var $selectdata = array();

    //メンバ変数の初期化
    function knjg082Model() {
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->getGroupDiv);
        }

        if (VARS::get("SEND_GROUP_DIV") != ""){
            $this->getGroupDiv = VARS::get("SEND_GROUP_DIV");
        }
        $this->field = array("GROUP_DIV"    =>  VARS::post("GROUP_DIV")
                             );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));
    }

    //追加
    function &getInsertModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->insCheck($db)) {
            return;
        }
        //登録しているGROUP_DIV削除
        $db->query(knjg082Query::deleteGroupDat($this));

        //LICENSE_GROUP_PRGID_DAT追加
        knjg082Query::insertGroupDatQuery($db, $this->selectdata, $this);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        $this->getGroupDiv = $this->field["GROUP_DIV"];

        return;
    }

    //追加チェック
    function insCheck($db) {
        //存在チェック
        if (0 < $db->getOne(knjg082Query::getGroupCnt($this->field["GROUP_DIV"]))) {
            $this->setWarning("MSG302");
            return false;
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->updCheck($db)) {
            return;
        }

        //登録しているGROUP_DIV削除
        $db->query(knjg082Query::deleteGroupDat($this));

        //LICENSE_GROUP_PRGID_DAT追加
        knjg082Query::insertGroupDatQuery($db, $this->selectdata, $this);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        $this->getGroupDiv = $this->field["GROUP_DIV"];

        return;
    }

    //更新チェック
    function updCheck($db) {
        //キー変更チェック
        if(($this->getGroupDiv != $this->field["GROUP_DIV"])) {
            $this->setWarning("MSG308");
            return false;
        }

        return true;
    }

    //削除
    function &getDeleteModel() {
        //キー変更チェック
        if(($this->getGroupDiv != $this->field["GROUP_DIV"])) {
            $this->setWarning("MSG308");
            return false;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //LICENSE_GROUP_PRGID_DAT削除
        $db->query(knjg082Query::deleteGroupDat($this));

        $db->commit();

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        unset($this->sendFlg);

        return;
    }
}
?>
