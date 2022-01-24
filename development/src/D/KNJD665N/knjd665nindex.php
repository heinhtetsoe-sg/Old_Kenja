<?php

require_once('for_php7.php');

require_once('knjd665nModel.inc');
require_once('knjd665nQuery.inc');

class knjd665nController extends Controller {
    var $ModelClassName = "knjd665nModel";
    var $ProgramID      = "KNJD665N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd665n":
                    $sessionInstance->knjd665nModel();
                    $this->callView("knjd665nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd665nCtl = new knjd665nController;
?>
