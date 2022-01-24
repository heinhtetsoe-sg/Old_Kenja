<?php

require_once('for_php7.php');

class knjz218Model extends Model {

    var $cmd;
    var $leftGrade;
    var $grade;
    var $reset_grade;
    var $type_group_cd;
    var $reset_type_group_cd;
    var $type_group_name;

    //メンバ変数の初期化
    function knjz218Model()
    {
    }

    function init()
    {

        $this->cmd = VARS::request("cmd");

        //初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->grade);
            unset($this->reset_grade);
            unset($this->type_group_cd);
            unset($this->reset_type_group_cd);
            unset($this->type_group_name);
            unset($this->leftGrade);
        }

        $this->grade            = VARS::request("GRADE");
        $this->type_group_cd    = VARS::request("TYPE_GROUP_CD");
        $this->type_group_name  = VARS::request("TYPE_GROUP_NAME");

        if ($this->cmd == "leftChange") {
            $this->leftGrade = VARS::post("LEFT_GRADE");
        }

        //取消用
        if (VARS::get("GRADE")) {
            $this->reset_grade = VARS::get("GRADE");
        }
        if (VARS::get("TYPE_GROUP_CD")) {
            $this->reset_type_group_cd = VARS::get("TYPE_GROUP_CD");
            unset($this->type_group_name);
        }

        //更新
        if ($this->cmd == "update" || $this->cmd == "insert") {
            $this->courseselect = VARS::post("courseselect");
        } else if ($this->cmd == "clear") {
            $this->grade            = $this->reset_grade;
            $this->type_group_cd    = $this->reset_type_group_cd;
            unset($this->type_group_name);
        }
    }

    //データコピー
    function &getCopyModel()
    {
        if (!$this->copyCheck()) {
            return;
        }

        knjz218Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    //コピーチェック
    function copyCheck()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $lastyear = CTRL_YEAR - 1;

        if (0 < $db->getOne(knjz218Query::checkTypeGroupM($lastyear))) {
            $this->setWarning("データが既に存在しています。");
            return false;
        }

        if (0 == $db->getOne(knjz218Query::checkTypeGroupM($lastyear, "LAST_DATA"))) {
            $this->setWarning("コピー対象データがありません。");
            return false;
        }

        if (0 == $db->getOne(knjz218Query::checkTypeGroupD($this, $lastyear, "COPY"))) {
            $this->setWarning("コピー対象データがありません。");
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

        //類型グループコースマスタ削除
        $db->query(knjz218Query::delTypeGroupMstQuery($this));

        //類型グループコースマスタ追加
        $db->query(knjz218Query::makeInsTypeGrpMQuery($this));

        //類型グループコースデータ追加
        knjz218Query::TypeGrpDInstQuery($db, $this->courseselect, $this);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }

    //追加チェック
    function insCheck($db)
    {
        if (0 < $db->getOne(knjz218Query::checkTypeGroupD($this, CTRL_YEAR, "INS"))) {
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

        //類型グループマスタ削除
        $db->query(knjz218Query::delTypeGroupMstQuery($this));

        //類型グループマスタ
        $db->query(knjz218Query::makeInsTypeGrpMQuery($this));

        //類型グループデータ削除
        $db->query(knjz218Query::delTypeGrpDQuery($this));

        //類型グループコース新規
        knjz218Query::TypeGrpDInstQuery($db, $this->courseselect, $this);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }

    //更新チェック
    function updCheck($db)
    {
        if (0 == $db->getOne(knjz218Query::checkTypeGroupD($this, CTRL_YEAR, "UPD"))) {
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
        $db->query(knjz218Query::delTypeGrpDQuery($this));

        if (0 == $db->getOne(knjz218Query::checkTypeGroupD($this, CTRL_YEAR, "DEL"))) {
            $db->query(knjz218Query::delTypeGroupMstQuery($this));
        }

        $db->commit();

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }
}
?>
