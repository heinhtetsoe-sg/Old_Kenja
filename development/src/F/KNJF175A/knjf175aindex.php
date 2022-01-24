<?php

require_once('for_php7.php');

require_once('knjf175aModel.inc');
require_once('knjf175aQuery.inc');

class knjf175aController extends Controller {
    var $ModelClassName = "knjf175aModel";
    var $ProgramID      = "KNJF175A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf175a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf175aModel();
                    $this->callView("knjf175aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf175aCtl = new knjf175aController;
?>
