<?php

require_once('for_php7.php');

require_once('knjp920Model.inc');
require_once('knjp920Query.inc');

class knjp920Controller extends Controller {
    var $ModelClassName = "knjp920Model";
    var $ProgramID      = "KNJP920";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjp920":
                case "clear":
                case "search":
                    $sessionInstance->knjp920Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp920Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp920Ctl = new knjp920Controller;
?>
