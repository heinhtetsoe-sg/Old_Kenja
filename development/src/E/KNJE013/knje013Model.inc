<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje013Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;
        
    /**
     * メンバ変数の初期化
     */
    function knje013Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "CHOICE"               =>  VARS::post("CHOICE"),               //出力指定
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "IDOU_DATE"            =>  VARS::post("IDOU_DATE"),            //異動日
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );
        $this->cmd         = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
    }

    //コピー
    function &getUpdateModel()
    {
        /*$db = Query::dbCheckOut();
        $dataCount  = $db->getOne(knje013Query::getHtrainremarkDetailData($this));
        Query::dbCheckIn($db);*/
        knje013Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
