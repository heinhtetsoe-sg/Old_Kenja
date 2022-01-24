<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl343jModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl343jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;
    }
    function init()
    {

        $this->field = array("DATE"             =>  VARS::post("DATE"),     	    //通知日付
							 "PRINT_RANGE"   	=>  VARS::post("PRINT_RANGE"),      //印刷範囲
							 "EXAMNO"   		=>  VARS::post("EXAMNO")    		//受験番号
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
