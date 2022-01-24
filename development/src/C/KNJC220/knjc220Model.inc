<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc220Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjc220Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }
    function init()
    {               
        $this->field = array("DATE_FROM"    =>  VARS::post("DATE_FROM"),    //開始日付
                             "DATE_TO"      =>  VARS::post("DATE_TO"),      //終了日付
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
