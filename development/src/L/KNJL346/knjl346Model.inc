<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl346Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl346Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
        $this->field = array("OUTPUT" =>  VARS::post("OUTPUT"));    		//印刷対象
    }
    function init()
    {               
        $this->field = array("APDIV"        	=>  VARS::post("APDIV"),     		//入試制度
                             "TESTSCR"        	=>  VARS::post("TESTSCR")     		//指定範囲
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
