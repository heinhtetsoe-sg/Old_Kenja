<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjg072Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjg072Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SCHREGNO"         =>  VARS::post("SCHREGNO"),         //生徒
                             "SEND_TO1"         =>  VARS::post("SEND_TO1"),         //宛名1
                             "SEND_TO2"         =>  VARS::post("SEND_TO2"),         //宛名2
                             "SEND_DATE"        =>  VARS::post("SEND_DATE"),        //発行日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
    }

    function getUpdateModel() {

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query = knjg072Query::getMaxSeq($this);
        $maxSeq = $db->getOne($query);
        $query = knjg072Query::updatePrintHist($this, $maxSeq);
        $db->query($query);
        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
