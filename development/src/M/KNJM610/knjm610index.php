<?php

require_once('for_php7.php');

require_once('knjm610Model.inc');
require_once('knjm610Query.inc');

class knjm610Controller extends Controller {
    var $ModelClassName = "knjm610Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "read":
                case "knjm610":
                    $sessionInstance->knjm610Model();
                    $this->callView("knjm610Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm610Ctl = new knjm610Controller;
var_dump($_REQUEST);
?>
