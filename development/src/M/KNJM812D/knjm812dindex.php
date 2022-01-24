<?php

require_once('for_php7.php');

require_once('knjm812dModel.inc');
require_once('knjm812dQuery.inc');

class knjm812dController extends Controller {
    var $ModelClassName = "knjm812dModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm812d":
                    $sessionInstance->knjm812dModel();
                    $this->callView("knjm812dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm812dCtl = new knjm812dController;
var_dump($_REQUEST);
?>
