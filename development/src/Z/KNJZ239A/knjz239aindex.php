<?php

require_once('for_php7.php');

require_once('knjz239aModel.inc');
require_once('knjz239aQuery.inc');

class knjz239aController extends Controller {
    var $ModelClassName = "knjz239aModel";
    var $ProgramID      = "KNJZ239A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz239a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjz239aModel();
                    $this->callView("knjz239aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz239aCtl = new knjz239aController;
?>
