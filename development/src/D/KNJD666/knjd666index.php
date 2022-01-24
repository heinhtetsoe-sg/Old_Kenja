<?php

require_once('for_php7.php');

require_once('knjd666Model.inc');
require_once('knjd666Query.inc');

class knjd666Controller extends Controller {
    var $ModelClassName = "knjd666Model";
    var $ProgramID      = "KNJD666";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd666":
                    $sessionInstance->knjd666Model();
                    $this->callView("knjd666Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd666Ctl = new knjd666Controller;
?>
