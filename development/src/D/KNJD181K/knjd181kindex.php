<?php

require_once('for_php7.php');

require_once('knjd181kModel.inc');
require_once('knjd181kQuery.inc');

class knjd181kController extends Controller {
    var $ModelClassName = "knjd181kModel";
    var $ProgramID      = "KNJD181K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd181k":
                    $sessionInstance->knjd181kModel();
                    $this->callView("knjd181kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd181kCtl = new knjd181kController;
?>
