<?php

require_once('for_php7.php');

require_once('knjxexp5Model.inc');
require_once('knjxexp5Query.inc');

class knjxexp5Controller extends Controller {
    var $ModelClassName = "knjxexp5Model";
    var $ProgramID      = "KNJXEXP5";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "right":
                    $this->callView("knjxSearch");
                    break 2;
                case "list":
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjxexp5Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("list");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxexp5Ctl = new knjxexp5Controller;
//var_dump($_REQUEST);
?>
