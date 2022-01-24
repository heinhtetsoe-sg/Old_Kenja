<?php

require_once('for_php7.php');

require_once('knjp952Model.inc');
require_once('knjp952Query.inc');

class knjp952Controller extends Controller {
    var $ModelClassName = "knjp952Model";
    var $ProgramID      = "KNJP952";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp952":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp952Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp952Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp952Ctl = new knjp952Controller;
?>
