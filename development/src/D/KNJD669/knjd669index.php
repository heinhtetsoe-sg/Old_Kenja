<?php

require_once('for_php7.php');

require_once('knjd669Model.inc');
require_once('knjd669Query.inc');

class knjd669Controller extends Controller {
    var $ModelClassName = "knjd669Model";
    var $ProgramID      = "KNJD669";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd669":
                    $sessionInstance->knjd669Model();
                    $this->callView("knjd669Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd669Ctl = new knjd669Controller;
?>
