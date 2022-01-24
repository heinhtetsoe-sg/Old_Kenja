<?php

require_once('for_php7.php');

class knjg084Model extends Model {
    var $cmd;
    var $field      = array();
    var $sendField  = array();
    var $selectdata = array();
    var $selectdata2 = array();

    //メンバ変数の初期化
    function knjg084Model() {
    }

    function init() {

        if (VARS::post("SCHKIND")) {
            $this->field["SCHKIND"] = VARS::post("SCHKIND");
        }
        if (VARS::get("SEL_GRADECLASS")) {
            $this->field["GRADE_CLASS"] = VARS::get("SEL_GRADECLASS");
        } else {
            if (VARS::post("SEL_GRADECLASS")) {
                $this->field["GRADE_CLASS"] = VARS::post("SEL_GRADECLASS");
            }
        }

        $this->cmd = VARS::request("cmd");

        $this->selectdata   = explode(",", VARS::post("selectdata"));
        $this->selectdata2  = explode(",", VARS::post("selectdata2"));

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //コピー
    function &InsertCopyLastYearModel() {
        if ($this->checkExistYearData()) {
            $this->setMessage("MSG302");
            return;
        }
        if (!$this->checkExistCopyData()) {
            $this->setMessage("MSG303");
            return;
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query(knjg084Query::insertCopyLastYearQuery($this));
        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        return;
    }

    //当年度データ存在チェック
    function checkExistYearData() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 1;
        $cnt = $db->getone(knjg084Query::checkYearDataQuery(CTRL_YEAR));
        Query::dbCheckIn($db);
        if ($cnt == "0") {
            return false;
        }
        return true;
    }

    //コピー元(昨年度)データ存在チェック
    function checkExistCopyData() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 1;
        $cnt = $db->getone(knjg084Query::cntExistCopyData($model));
        Query::dbCheckIn($db);
        if ($cnt == "0") {
            return false;
        }
        return true;
    }

    //更新
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データ削除
        $query = knjg084Query::deleteNotSenderQuery($db,$this,$this->selectdata);
        $db->query($query);
        $db->commit(); 

        Query::dbCheckIn($db);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データ更新
        foreach ($this->selectdata as $val) {
            $query = knjg084Query::updateNotSenderQuery($db,$this,$val);
            $db->query($query);
            $db->commit();
        }

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");

        return;
    }
}
?>
