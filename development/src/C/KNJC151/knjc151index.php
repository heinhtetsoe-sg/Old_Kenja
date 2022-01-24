<?php

require_once('for_php7.php');

require_once('knjc151Model.inc');
require_once('knjc151Query.inc');

class knjc151Controller extends Controller {
    var $ModelClassName = "knjc151Model";
    var $ProgramID      = "KNJC151";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc151":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc151Model();        //コントロールマスタの呼び出し
                    $this->callView("knjc151Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc151Ctl = new knjc151Controller;
//var_dump($_REQUEST);
?>
