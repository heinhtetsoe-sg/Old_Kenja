<?php

require_once('for_php7.php');

require_once('knjd677Model.inc');
require_once('knjd677Query.inc');

class knjd677Controller extends Controller {
    var $ModelClassName = "knjd677Model";
    var $ProgramID      = "KNJD677";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd677":
                    $sessionInstance->knjd677Model();
                    $this->callView("knjd677Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd677Ctl = new knjd677Controller;
?>
