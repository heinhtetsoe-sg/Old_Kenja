<?php

require_once('for_php7.php');

require_once('knjc190Model.inc');
require_once('knjc190Query.inc');

class knjc190Controller extends Controller {
    var $ModelClassName = "knjc190Model";
    var $ProgramID      = "KNJC190";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjc190Model();       //コントロールマスタの呼び出し
                    $this->callView("knjc190Form1");
                    exit;
                case "knjc190":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjc190Model();       //コントロールマスタの呼び出し
                    $this->callView("knjc190Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc190Ctl = new knjc190Controller;
var_dump($_REQUEST);
?>
