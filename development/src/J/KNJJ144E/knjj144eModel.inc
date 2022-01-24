<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj144eModel extends Model {
    var $field = array();
    var $cmd;
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjj144eModel() {
    }

    function init() {
        $this->field = array("DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function getExecModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjj144eQuery::deleteMarathonEventRankDat($db);
        knjj144eQuery::insertMarathonEventRankDat($db);

        $db->commit();
        Query::dbCheckIn($db);
    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
