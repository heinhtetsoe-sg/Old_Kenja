<?php

require_once('for_php7.php');

require_once('knjp965Model.inc');
require_once('knjp965Query.inc');

class knjp965Controller extends Controller {
    var $ModelClassName = "knjp965Model";
    var $ProgramID      = "KNJP965";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp965":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp965Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp965Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp965Ctl = new knjp965Controller;
?>
