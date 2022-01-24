<?php

require_once('for_php7.php');

require_once('knjd296Model.inc');
require_once('knjd296Query.inc');

class knjd296Controller extends Controller {
    var $ModelClassName = "knjd296Model";
    var $ProgramID      = "KNJD296";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd296":
                    $sessionInstance->knjd296Model();
                    $this->callView("knjd296Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd296Ctl = new knjd296Controller;
?>
