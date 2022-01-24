<?php

require_once('for_php7.php');

require_once('knjh120Model.inc');
require_once('knjh120Query.inc');

class knjh120Controller extends Controller {
    var $ModelClassName = "knjh120Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh120":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh120Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh120Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjh120Ctl = new knjh120Controller;
var_dump($_REQUEST);
?>
