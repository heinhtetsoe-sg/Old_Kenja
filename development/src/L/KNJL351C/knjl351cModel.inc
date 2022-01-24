<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl351cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl351cModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "PRINT_TYPE"	=>  VARS::post("PRINT_TYPE")        //帳票種類
                            );

        $this->cmd         = VARS::request("cmd");

        $rtnRow = knjl351cQuery::getNameMstZ010();
        $this->isWakayama = $rtnRow["NAME2"] == "30300049001" ? true : false;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
