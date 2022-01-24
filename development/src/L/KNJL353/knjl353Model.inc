<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl353Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    function knjl353Model()
    {
    }
    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV")
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
