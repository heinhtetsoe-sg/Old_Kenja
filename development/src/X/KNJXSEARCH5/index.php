<?php

require_once('for_php7.php');

require_once('knjxsearch5Model.inc');
require_once('knjxsearch5Query.inc');

class knjxsearch5Controller extends Controller {
    var $ModelClassName = "knjxsearch5Model";
    var $ProgramID      = "KNJXSEARCH5";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "right":
                    $this->callView("knjxsearch5");
                    break 2;
                case "list":
                case "search":
                    $this->callView("knjxsearch5Form1");
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
$knjxsearch5Ctl = new knjxsearch5Controller;
//var_dump($_REQUEST);
?>
