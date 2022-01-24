<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd321bModel extends Model {
    var $field      = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjd321bModel()
    {
    }

    function init()
    {
        $this->field = array("GRADE"             =>  VARS::post("GRADE"),            //学年
                             "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //欠課時数によりA～Cパターンを０で更新する。
        //ATTEND_SUBCLASS_OVER_DATの更新
        $this->updatePatern0($db);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

    //欠課時数によりA～Cパターンを０で更新する。
    function updatePatern0($db) {
        //ATTEND_SUBCLASS_OVER_DAT削除
        $query=knjd321bQuery::getDeleteSubclassOverDat($this);
        $db->query($query);

        $query=knjd321bQuery::getPatern0Taisyou($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //生徒毎A～Cパターンを０で更新する。
            $query=knjd321bQuery::getPatern0Update($this, $row);
            $db->query($query);

            //ATTEND_SUBCLASS_OVER_DAT作成
            $query=knjd321bQuery::getInsertSubclassOverDat($this, $row);
            $db->query($query);
        }
    }
}
?>
