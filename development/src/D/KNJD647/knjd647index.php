<?php

require_once('for_php7.php');

require_once('knjd647Model.inc');
require_once('knjd647Query.inc');

class knjd647Controller extends Controller {
    var $ModelClassName = "knjd647Model";
    var $ProgramID      = "KNJD647";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd647":
                    $sessionInstance->knjd647Model();
                    $this->callView("knjd647Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd647Ctl = new knjd647Controller;
var_dump($_REQUEST);
?>
