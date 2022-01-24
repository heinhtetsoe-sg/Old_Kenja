<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl347Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl347Model()
    {
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"), //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),      //入試区分
                             "TARGET_DIV"       =>  VARS::post("TARGET_DIV"),   //帳票パターン
                             "SAITEI_NODISP"    =>  VARS::post("SAITEI_NODISP"),//最低点印字
                             "EXAMNO"           =>  VARS::post("EXAMNO")        //受験番号
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
