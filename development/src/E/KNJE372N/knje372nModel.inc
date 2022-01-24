<?php
class knje372nModel extends Model {

    var $cmd;
    var $field = array();
    var $oyear;
    var $ryear;
    var $selectLeft;
    var $selectLeftText;
    var $recommendation_cd;
    var $seq;
    var $assess_avg;
    var $classcd;
    var $school_kind;

    function knje372nModel()
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
                             "SEQ"               => VARS::post("SEQ"),
                             "ASSESS_AVG"        => VARS::post("ASSESS_AVG"),
                             "CLASSCD"           => substr(VARS::post("CLASSCD"), 0, 2),
                             "SCHOOL_KIND"       => substr(VARS::post("CLASSCD"), 2, 1));


        $this->selectLeft     = VARS::post("selectLeft");
        $this->selectLeftText = VARS::post("selectLeftText");

        //左画面から
        if (VARS::get("RECOMMENDATION_CD") || VARS::get("SEQ") || VARS::get("ASSESS_AVG") || VARS::get("CLASSCD")) {
            $this->field["RECOMMENDATION_CD"] = $this->recommendation_cd = VARS::get("RECOMMENDATION_CD");
            $this->field["SEQ"]               = $this->seq               = VARS::get("SEQ");
            $this->field["ASSESS_AVG"]        = VARS::get("ASSESS_AVG");
            $this->field["CLASSCD"]           = substr(VARS::get("CLASSCD"), 0, 2);
            $this->field["SCHOOL_KIND"]       = substr(VARS::get("CLASSCD"), 2, 1);
        }
    }

    //入力項目チェック
    function validateInput($db)
    {
        //件数チェック
        $cnt = $db->getone(knje372nQuery::getCountData($this));
        //追加時の重複チェック
        if ($this->cmd == "add" && $cnt > 0) {
            $this->setWarning("MSG302");
            return false;
        }

        //平均点のチェック用
        if($this->cmd == "add" || $this->cmd == "update"){
            $assess_avg = explode(".",$this->field["ASSESS_AVG"]);
            if (isset($assess_avg[1])) {
                //小数点がある場合
                if(strlen($assess_avg[1]) > 1){
                    $this->setWarning("( 平均点 ) 小数1桁まで");
                    return false;
                }
            }
        }

        //更新時、削除時の存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && $cnt == 0) {
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
            if($this->cmd == "add"){
                //最大SEQ取得
                $this->field["SEQ"] = $db->getone(knje372nQuery::getMaxSeq($this)) +1 ;
            }

            //削除処理
            knje372nQuery::getDeleteQuery($this, $db, "AFT_RECOMMENDATION_ASSESS_AVG_DAT");

            //削除処理
            knje372nQuery::getDeleteQuery($this, $db, "AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT");
            
            //追加処理(AFT_RECOMMENDATION_ASSESS_AVG_DAT)
            knje372nQuery::getUpdateQueryAvg($this, $db);
            
            //追加処理(AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT)
            knje372nQuery::getUpdateQuerySubclass($this, $db);

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

            //削除処理
            knje372nQuery::getDeleteQuery($this, $db, "AFT_RECOMMENDATION_ASSESS_AVG_DAT");

            //削除処理
            knje372nQuery::getDeleteQuery($this, $db, "AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT");

            $this->field["RECOMMENDATION_CD"] = '';
            $this->field["SEQ"]               = '';
            $this->field["ASSESS_AVG"]        = '';
            $this->field["CLASSCD"]           = '';
            $this->field["SCHOOL_KIND"]       = '';
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
        if (0 < count($db->getCol(knje372nQuery::getSelectQuery($this, "CNT")))) {
            $this->setWarning("対象年度のデータが既に存在しています。");
            return false;
        }

        //対象年度データ取得(AFT_RECOMMENDATION_ASSESS_AVG_DAT)
        $query = knje372nQuery::getCopyDataAvg($this->ryear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //件数チェック
            $cnt = count($db->getCol(knje372nQuery::getAftRecommendationLimitMst($this->oyear, $row["RECOMMENDATION_CD"])));
            if($cnt > 0){

                //年度コピー
                knje372nQuery::getCopyQueryAvg($this, $row, $db);
                $exexCnt = $exexCnt + 1;

                //対象年度データ取得(AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT)
                $query = knje372nQuery::getCopyDataSubclass($this->ryear, $row);
                $resultSubclass = $db->query($query);
                while ($subclassRow = $resultSubclass->fetchRow(DB_FETCHMODE_ASSOC)) {

                    //件数チェック
                    $cnt = count($db->getCol(knje372nQuery::getAftRecommendationLimitMst($this->oyear, $subclassRow["RECOMMENDATION_CD"])));
                    if($cnt > 0){

                        //件数チェック
                        $cnt = count($db->getCol(knje372nQuery::getSubclassList($this, $subclassRow, "CHECK")));
                        if($cnt > 0){

                            //年度コピー
                            knje372nQuery::getCopyQuerySubclass($this, $subclassRow, $db);
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
