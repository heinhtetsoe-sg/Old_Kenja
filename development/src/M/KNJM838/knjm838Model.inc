<?php

require_once('for_php7.php');


/**
 * 定数定義
*/

class knjm838Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm838Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"               =>  VARS::post("YEAR"),               //年度
                             "SEISEKI_KAIGI_DATE" =>  VARS::post("SEISEKI_KAIGI_DATE"), //前期成績会議日付
                             "DBNAME"             =>  VARS::post("DBNAME")              //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
    }
    
}
?>

