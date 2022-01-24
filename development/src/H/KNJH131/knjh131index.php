<?php

require_once('for_php7.php');

require_once('knjh131Model.inc');
require_once('knjh131Query.inc');

class knjh131Controller extends Controller {
    var $ModelClassName = "knjh131Model";
    var $ProgramID      = "KNJH131";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh131":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh131Model();   //コントロールマスタの呼び出し
                    $this->callView("knjh131Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh131Ctl = new knjh131Controller;
var_dump($_REQUEST);
?>
