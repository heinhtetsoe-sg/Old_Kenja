<?php

require_once('for_php7.php');

require_once('knjm807Model.inc');
require_once('knjm807Query.inc');

class knjm807Controller extends Controller {
    var $ModelClassName = "knjm807Model";
    var $ProgramID      = "KNJM807";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "knjm807":
                    $sessionInstance->knjm807Model();
                    $this->callView("knjm807Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm807Ctl = new knjm807Controller;
var_dump($_REQUEST);
?>
