<?php

require_once('for_php7.php');

require_once('knjc039Model.inc');
require_once('knjc039Query.inc');

class knjc039Controller extends Controller {
    var $ModelClassName = "knjc039Model";
    var $ProgramID      = "KNJC039";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc039":
                    $sessionInstance->knjc039Model();
                    $this->callView("knjc039Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc039Ctl = new knjc039Controller;
?>
