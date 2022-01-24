<?php

require_once('for_php7.php');

require_once('knjmp952Model.inc');
require_once('knjmp952Query.inc');

class knjmp952Controller extends Controller {
    var $ModelClassName = "knjmp952Model";
    var $ProgramID      = "KNJMP952";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp952":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp952Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp952Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp952Ctl = new knjmp952Controller;
?>
