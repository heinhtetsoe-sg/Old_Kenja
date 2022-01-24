<?php

require_once('for_php7.php');

require_once('knjf035Model.inc');
require_once('knjf035Query.inc');

class knjf035Controller extends Controller {
    var $ModelClassName = "knjf035Model";
    var $ProgramID      = "KNJF035";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf035":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf035Model();      //コントロールマスタの呼び出し
                    $this->callView("knjf035Form1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO001
                    $sessionInstance->knjf035Model();      //コントロールマスタの呼び出し
                    $this->callView("knjf035Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf035Ctl = new knjf035Controller;
//var_dump($_REQUEST);
?>
