<?php

require_once('for_php7.php');

require_once('knjf175bModel.inc');
require_once('knjf175bQuery.inc');

class knjf175bController extends Controller {
    var $ModelClassName = "knjf175bModel";
    var $ProgramID      = "KNJF175B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf175b":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf175bModel();
                    $this->callView("knjf175bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf175bCtl = new knjf175bController;
?>
