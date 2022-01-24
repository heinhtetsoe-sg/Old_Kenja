<?php
class knje372oModel extends Model {

    var $cmd;
    var $field = array();
    var $oyear;
    var $ryear;
    var $selectLeft;
    var $selectLeftText;
    var $recommendation_cd;
    var $coursemajor;
    var $coursecode;
    var $classcd;
    var $school_kind;

    function knje372oModel()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->ryear = (VARS::request("RYEAR")) ? VARS::request("RYEAR") : $this->ryear;
        $this->oyear = (VARS::request("OYEAR")) ? VARS::request("OYEAR") : $this->oyear;


        if (VARS::get("PROGRAMID")) {
            unset($this->ryear);
            unset($this->oyear);
        }

        $this->field = array("RECOMMENDATION_CD" => VARS::post("RECOMMENDATION_CD"),
                             "COURSEMAJOR"       => VARS::post("COURSEMAJOR"),
                             "COURSECODE"        => VARS::post("COURSECODE"),
                             "CLASSCD"           => substr(VARS::post("CLASSCD"), 0, 2),
                             "SCHOOL_KIND"       => substr(VARS::post("CLASSCD"), 2, 1));

        $this->selectLeft     = VARS::post("selectLeft");
        $this->selectLeftText = VARS::post("selectLeftText");

        //左画面から
        if (VARS::get("RECOMMENDATION_CD") || VARS::get("COURSEMAJOR") || VARS::get("COURSECODE") || VARS::get("CLASSCD")) {
            $this->field["RECOMMENDATION_CD"] = $this->recommendation_cd = VARS::get("RECOMMENDATION_CD");
            $this->field["COURSEMAJOR"]       = $this->coursemajor       = VARS::get("COURSEMAJOR");
            $this->field["COURSECODE"]        = $this->coursecode        = VARS::get("COURSECODE");
            $this->field["CLASSCD"]           = $this->classcd           = substr(VARS::get("CLASSCD"), 0, 2);
            $this->field["SCHOOL_KIND"]       = $this->school_kind       = substr(VARS::get("CLASSCD"), 2, 1);
        }
    }

    //入力項目チェック
    function validateInput($db)
    {
        $query = knje372oQuery::getSelectData($this);
        $row = $db->getRow($query);
        //追加時の重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG302");
            return false;
        }

        //更新時、削除時の存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        return ture;
    }

    //更新処理
    function &getUpdateModel(){

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //入力チェック
        if($this->validateInput($db)){

            knje372oQuery::getDeleteQuery($this, $db);
            knje372oQuery::getUpdateQuery($this, $db);

            $this->setMessage("MSG201");
        }
        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //入力チェック
        if($this->validateInput($db)){

            knje372oQuery::getDeleteQuery($this, $db);

            $this->field["RECOMMENDATION_CD"] = '';
            $this->field["COURSEMAJOR"]       = '';
            $this->field["COURSECODE"]        = '';
            $this->field["CLASSCD"]           = '';
            $this->setMessage("MSG201");
        }
        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return;
    }


    //コピーチェック
    function &getCopyModel()
    {
        //実行件数
        $exexCnt = 0; 

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //参照年度・対象年度チェック     
        if ($this->ryear == $this->oyear) {
            $this->setWarning("MSG203", "同年度のコピーは出来ません。");
            return false;
        }
        //対象年度のデータ存在チェック
        if (0 < count($db->getCol(knje372oQuery::getSelectQuery($this, "CNT")))) {
            $this->setWarning("対象年度のデータが既に存在しています。");
            return false;
        }

        //対象年度データ取得
        $query = knje372oQuery::getCopyData($this->ryear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //推薦枠チェック
            $cnt = count($db->getCol(knje372oQuery::getAftRecommendationLimitMst($this->oyear, $row, 'CHECK')));
            if($cnt > 0){
                //課程学科チェック
                $cnt = count($db->getCol(knje372oQuery::getCourseMajorMst($this->oyear, $row, 'CHECK')));
                if($cnt > 0){
                    //コースチェック
                    $cnt = count($db->getCol(knje372oQuery::getCourseCodeMst($this->oyear, $row, 'CHECK')));
                    if($cnt > 0){
                        //科目チェック
                        $cnt = count($db->getCol(knje372oQuery::getSubclassList($this, $row, 'CHECK')));
                        if($cnt > 0){
                            knje372oQuery::getCopyQuery($this, $row, $db);
                            $exexCnt = $exexCnt + 1;
                        }
                    }
                }
            }
        }

        if($exexCnt > 0){
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("対象のデータはありません");
        }
        //コミット
        $db->commit();
        //DB切断
        Query::dbCheckIn($db);

        return;
    }


}
?>
