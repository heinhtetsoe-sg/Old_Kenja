<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl310Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl310Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;                  //職員コード
    }
    function init()
    {               
        $this->field = array("APDIV"			=>  VARS::post("APDIV"),		//入試制度
                             "TESTDV"			=>  VARS::post("TESTDV"),		//入試区分
                             "EXAM_TYPE"		=>  VARS::post("EXAM_TYPE")		//受験型
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
