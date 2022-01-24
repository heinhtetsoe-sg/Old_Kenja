<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl351bModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl351bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV")         //入試区分
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