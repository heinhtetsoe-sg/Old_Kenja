<?php

require_once('for_php7.php');

require_once('knjc130Model.inc');
require_once('knjc130Query.inc');

class knjc130Controller extends Controller {
    var $ModelClassName = "knjc130Model";
    var $ProgramID      = "KNJC130";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjc130Model();       //コントロールマスタの呼び出し
                    $this->callView("knjc130Form1");
                    exit;
                case "knjc130":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc130Model();       //コントロールマスタの呼び出し
                    $this->callView("knjc130Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc130Ctl = new knjc130Controller;
var_dump($_REQUEST);
?>
