<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl307bModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl307bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "EXAMNO_FROM"  =>  VARS::post("EXAMNO_FROM"),    //受験番号FROM
                             "EXAMNO_TO"    =>  VARS::post("EXAMNO_TO")       //受験番号TO
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
