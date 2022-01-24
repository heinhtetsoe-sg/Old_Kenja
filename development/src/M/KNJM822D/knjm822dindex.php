<?php

require_once('for_php7.php');

require_once('knjm822dModel.inc');
require_once('knjm822dQuery.inc');

class knjm822dController extends Controller {
    var $ModelClassName = "knjm822dModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm822d":
                    $sessionInstance->knjm822dModel();
                    $this->callView("knjm822dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm822dCtl = new knjm822dController;
var_dump($_REQUEST);
?>
