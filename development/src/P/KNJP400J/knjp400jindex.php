<?php

require_once('for_php7.php');

require_once('knjp400jModel.inc');
require_once('knjp400jQuery.inc');

class knjp400jController extends Controller {
    var $ModelClassName = "knjp400jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp400j":
                    $sessionInstance->knjp400jModel();
                    $this->callView("knjp400jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp400jCtl = new knjp400jController;
var_dump($_REQUEST);
?>
