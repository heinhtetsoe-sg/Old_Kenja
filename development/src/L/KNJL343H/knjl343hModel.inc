<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl343hModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testdiv;
        
    /**
     * メンバ変数の初期化
     */
    function knjl343hModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("NOTICEDAY"        =>  VARS::post("NOTICEDAY"),     	//通知日付
							 "OUTPUT"   		=>  VARS::post("OUTPUT"),    		//出力範囲
							 "EXAMNO"   		=>  VARS::post("EXAMNO"),    		//受験番号
                     );
        $this->testdiv  = VARS::post("TESTDIV");

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
