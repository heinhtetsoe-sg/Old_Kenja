<?php

require_once('for_php7.php');

class knjh334Model extends Model {

    var $cmd;
    var $ryear;
    var $oyear;
    var $groupcd;
    var $resetgroupcd;
    var $usergroup;
    var $mockselect;
    var $userselect;

    //メンバ変数の初期化
    function knjh334Model()
    {
    }

    function init()
    {

        $this->cmd = VARS::request("cmd");

        //初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->ryear);
            unset($this->oyear);
            unset($this->groupcd);
            unset($this->resetgroupcd);
            unset($this->usergroup);
            unset($this->mockselect);
            unset($this->userselect);
        }

        $this->ryear = (VARS::request("RYEAR")) ? VARS::request("RYEAR") : $this->ryear;
        $this->oyear = (VARS::request("OYEAR")) ? VARS::request("OYEAR") : $this->oyear;

        if (VARS::request("GROUPCD")) {
            $this->groupcd = VARS::request("GROUPCD");
        }

        if (VARS::request("USERGROUP")) {
            $this->usergroup = VARS::request("USERGROUP");
        }

        //取消用
        if (VARS::get("GROUPCD")) {
            $this->resetgroupcd = VARS::get("GROUPCD");
        }

        //更新
        if ($this->cmd == "update" || $this->cmd == "insert") {
            $this->mockselect = VARS::post("mockselect");
            $this->userselect = VARS::post("userselect");
        } else if ($this->cmd == "clear") {
            $this->groupcd = ($this->resetgroupcd) ? $this->resetgroupcd : $this->groupcd;
        } else if ($this->cmd == "changeOyear") {
            unset($this->groupcd);
            unset($this->usergroup);
        }
    }

    //データコピー
    function &getCopyModel()
    {
        if (!$this->copyCheck()) {
            return;
        }
        knjh334Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    //コピーチェック
    function copyCheck()
    {
        //DB接続
        $db = Query::dbCheckOut();

        if (0 < $db->getOne(knjh334Query::checkMockGroup($this->oyear, $this))) {
            $this->setWarning("対象年度のデータが既に存在しています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    //追加
    function &getInsertModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->insCheck($db)) {
            return;
        }

        //模試グループ1新規
        knjh334Query::getMockGInstQuery($db, $this->mockselect, $this, 0);

        //模試グループ2新規
        knjh334Query::getMockGInstQuery($db, $this->userselect, $this, 2);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }

    //追加チェック
    function insCheck($db)
    {
        if (0 < $db->getOne(knjh334Query::checkMockGroup($this->oyear, $this, $this->groupcd))) {
            $this->setWarning("データが既に存在しています。");
            return false;
        }

        return true;
    }

    //更新
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->updCheck($db)) {
            return;
        }

        //模試グループ1削除
        $db->query(knjh334Query::makeDelMocGQuery($this, 0));
        //模試グループ1新規
        knjh334Query::getMockGInstQuery($db, $this->mockselect, $this, 0);

        //模試グループ2削除
        $db->query(knjh334Query::makeDelMocGQuery($this, 2));
        //模試グループ2新規
        knjh334Query::getMockGInstQuery($db, $this->userselect, $this, 2);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }

    //更新チェック
    function updCheck($db)
    {
        if (0 == $db->getOne(knjh334Query::checkMockGroup($this->oyear, $this, $this->groupcd))) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //削除
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //グループデータ
        $db->query(knjh334Query::makeDelMocGQuery($this, 0));
        $db->query(knjh334Query::makeDelMocGQuery($this, 2));

        $db->commit();

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }
}
?>
