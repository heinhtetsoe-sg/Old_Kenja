<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl327jModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl327jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("TESTDIV"      =>  VARS::post("TESTDIV"),     	//入試区分
                             "DATE_YEAR"    =>  VARS::post("DATE_YEAR"),    //通知年
                             "DATE_MONTH"   =>  VARS::post("DATE_MONTH"),   //通知月
                             "PRINT_TYPE"   =>  VARS::post("PRINT_TYPE"),   //帳票種類
							 "PRINT_RANGE"  =>  VARS::post("PRINT_RANGE"),  //出力範囲
							 "EXAMNO_FROM"  =>  VARS::post("EXAMNO_FROM"),  //受験番号開始
							 "EXAMNO_TO"   	=>  VARS::post("EXAMNO_TO")    	//受験番号終了
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
