<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl329Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl329Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("TESTDV"           =>  VARS::post("TESTDV"),     		//入試制度
                             "APDIV"        	=>  VARS::post("APDIV"),     		//入試区分
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
