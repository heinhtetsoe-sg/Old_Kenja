<?php

require_once('for_php7.php');

require_once('knjp910Model.inc');
require_once('knjp910Query.inc');

class knjp910Controller extends Controller {
    var $ModelClassName = "knjp910Model";
    var $ProgramID      = "KNJP910";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjp910":
                case "clear":
                case "search":
                    $sessionInstance->knjp910Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp910Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp910Ctl = new knjp910Controller;
?>
