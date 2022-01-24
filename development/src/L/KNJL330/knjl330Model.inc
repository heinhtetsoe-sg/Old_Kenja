<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl330Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl330Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("TESTDV"           =>  VARS::post("TESTDV"),     		//入試制度
                             "APDIV"     		=>  VARS::post("APDIV"),     		//入試区分
                             "TESTSCR"        	=>  VARS::post("TESTSCR"),     		//指定範囲(２科目合計未満)
                             "TESTSCR2"        	=>  VARS::post("TESTSCR2"),     	//指定範囲(４科目合計)
                             "TESTSCR2"        	=>  VARS::post("TESTSCR3"),     	//指定範囲(２科目合計以上)
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
							 "OUTPUT"   		=>  VARS::post("OUTPUT"),    		//印刷対象
							 "POROW"   			=>  VARS::post("POROW"),    		//行
							 "POCOL"   			=>  VARS::post("POCOL")    			//列
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
