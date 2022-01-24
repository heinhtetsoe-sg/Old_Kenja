<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl306jModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl306jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("TEST_DATE1_FROM"     =>  VARS::post("TEST_DATE1_FROM"),     //一般出願開始日
                             "TEST_DATE1_TO"       =>  VARS::post("TEST_DATE1_TO"),       //一般出願開始日
                             "TEST_DATE2_FROM"     =>  VARS::post("TEST_DATE2_FROM"),     //帰国生出願開始日
                             "TEST_DATE2_TO"       =>  VARS::post("TEST_DATE2_TO")        //帰国生出願開始日
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
