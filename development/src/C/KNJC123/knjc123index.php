<?php

require_once('for_php7.php');

require_once('knjc123Model.inc');
require_once('knjc123Query.inc');

class knjc123Controller extends Controller {
    var $ModelClassName = "knjc123Model";
    var $ProgramID      = "KNJC123";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc123":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc123Model();       //コントロールマスタの呼び出し
                    $this->callView("knjc123Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjc123Ctl = new knjc123Controller;
var_dump($_REQUEST);
?>
