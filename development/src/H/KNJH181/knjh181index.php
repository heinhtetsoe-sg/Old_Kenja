<?php

require_once('for_php7.php');

require_once('knjh181Model.inc');
require_once('knjh181Query.inc');

class knjh181Controller extends Controller {
    var $ModelClassName = "knjh181Model";
    var $ProgramID    = "KNJH181";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh181":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh181Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh181Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjh181Ctl = new knjh181Controller;
var_dump($_REQUEST);
?>
