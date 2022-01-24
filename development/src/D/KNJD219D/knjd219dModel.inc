<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd219dModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata = array();

    /**
     * メンバ変数の初期化
     */
    function knjd219dModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"            =>  VARS::post("GRADE"),        //学年
                             "SELECT_DIV"       =>  VARS::post("SELECT_DIV")    //クラスOR学年
                             );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        //プロパティファイル
        $this->getPropertiesAll();
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        knjd219dQuery::setDelIns($db, $this);

        $db->commit();

        //DB切断
        Query::dbCheckIn($db);
        $this->setMessage("MSG202");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
