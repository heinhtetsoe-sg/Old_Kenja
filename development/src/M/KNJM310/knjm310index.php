<?php

require_once('for_php7.php');

require_once('knjm310Model.inc');
require_once('knjm310Query.inc');

class knjm310Controller extends Controller {
    var $ModelClassName = "knjm310Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm310":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm310Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm310Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm310Ctl = new knjm310Controller;
var_dump($_REQUEST);
?>
