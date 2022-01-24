<?php

require_once('for_php7.php');

class knjz218aModel extends Model {
    var $cmd;
    var $field      = array();
    var $sendField  = array();
    var $selectdata = array();

    //メンバ変数の初期化
    function knjz218aModel() {
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->sendFlg);
            $this->sendField = array();
        }

        $this->sendFlg = VARS::get("SEND_FLG");

        if ($this->sendFlg) {
            $this->sendField = array("GRADE"    =>  VARS::get("GRADE"),
                                     "GROUP_CD" =>  VARS::get("GROUP_CD")
                                     );
        }

        $this->field = array("GRADE"        =>  VARS::post("GRADE"),
                             "GROUP_CD"     =>  (VARS::post("GROUP_CD")) ? sprintf("%03d", VARS::post("GROUP_CD")) : "",
                             "GROUP_NAME"   =>  VARS::post("GROUP_NAME"),
                             "GROUP_ABBV"   =>  VARS::post("GROUP_ABBV")
                             );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));
    }

    //データコピー
    function &getCopyModel() {
        if (!$this->copyCheck()) {
            return;
        }

        knjz218aQuery::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    //コピーチェック
    function copyCheck() {
        $db = Query::dbCheckOut();

        $lastyear = CTRL_YEAR - 1;

        if (0 < $db->getOne(knjz218aQuery::checkCourseGroupHdata($this, $lastyear))) {
            $this->setWarning("データが既に存在しています。");
            return false;
        }
        if (0 == $db->getOne(knjz218aQuery::checkCourseGroupHdata($this, $lastyear, "LAST_DATA"))) {
            $this->setWarning("コピー対象データがありません。");
            return false;
        }
        if (0 == $db->getOne(knjz218aQuery::checkCourseGroupData($this, $lastyear, "COPY"))) {
            $this->setWarning("コピー対象データがありません。");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    //追加
    function &getInsertModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェック処理
        if (!$this->insCheck($db)) {
            return;
        }

        //COURSE_GROUP_HDAT削除
        $db->query(knjz218aQuery::deleteCourseGroupHdat($this));

        //COURSE_GROUP_HDAT追加
        $db->query(knjz218aQuery::insertCourseGroupHdat($this));

        //COURSE_GROUP_DAT削除
        $db->query(knjz218aQuery::deleteCourseGroupDat($this));

        //COURSE_GROUP_DAT追加
        knjz218aQuery::insertCourseGroupDatQuery($db, $this->selectdata, $this);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        unset($this->sendFlg);

        return;
    }

    //追加チェック
    function insCheck($db) {
        //存在チェック
        if (0 < $db->getOne(knjz218aQuery::checkCourseGroupData($this, CTRL_YEAR, "INSERT"))) {
            $this->setWarning("データが既に存在しています。");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["GROUP_NAME"]) > 60) {
            $this->setWarning("MSG914", "(コースグループ名称) 60バイトまで");
            return false;
        } elseif (strlen($this->field["GROUP_ABBV"]) > 60){ 
            $this->setWarning("MSG914", "(コースグループ略称) 60バイトまで");
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

        //COURSE_GROUP_HDAT削除
        $db->query(knjz218aQuery::deleteCourseGroupHdat($this));

        //COURSE_GROUP_HDAT追加
        $db->query(knjz218aQuery::insertCourseGroupHdat($this));

        //COURSE_GROUP_DAT削除
        $db->query(knjz218aQuery::deleteCourseGroupDat($this));

        //COURSE_GROUP_DAT追加
        knjz218aQuery::insertCourseGroupDatQuery($db, $this->selectdata, $this);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        unset($this->sendFlg);

        return;
    }

    //更新チェック
    function updCheck($db) {
        //キー変更チェック
        if(($this->sendField["GRADE"] != $this->field["GRADE"]) || ($this->sendField["GROUP_CD"] != $this->field["GROUP_CD"])) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        if (0 == $db->getOne(knjz218aQuery::checkCourseGroupData($this, CTRL_YEAR, "UPDATE"))) {
            $this->setWarning("MSG303");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["GROUP_NAME"]) > 30) {
            $this->setWarning("MSG914", "(コースグループ名称) 30バイトまで");
            return false;
        } elseif (strlen($this->field["GROUP_ABBV"]) > 15){ 
            $this->setWarning("MSG914", "(コースグループ略称) 15バイトまで");
            return false;
        }

        return true;
    }

    //削除
    function &getDeleteModel() {
        //キー変更チェック
        if(($this->sendField["GRADE"] != $this->field["GRADE"]) || ($this->sendField["GROUP_CD"] != $this->field["GROUP_CD"])) {
            $this->setWarning("MSG308");
            return;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //COURSE_GROUP_DAT削除
        $db->query(knjz218aQuery::deleteCourseGroupDat($this));

        if (0 == $db->getOne(knjz218aQuery::checkCourseGroupData($this, CTRL_YEAR, "DELETE"))) {
            //COURSE_GROUP_HDAT削除
            $db->query(knjz218aQuery::deleteCourseGroupHdat($this));
        }

        $db->commit();

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        unset($this->sendFlg);

        return;
    }
}
?>
