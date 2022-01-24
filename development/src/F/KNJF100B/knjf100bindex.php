<?php

require_once('for_php7.php');

require_once('knjf100bModel.inc');
require_once('knjf100bQuery.inc');

class knjf100bController extends Controller {
    var $ModelClassName = "knjf100bModel";
    var $ProgramID      = "KNJF100B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf100b":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjf100bModel();
                    $this->callView("knjf100bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf100bCtl = new knjf100bController;
//var_dump($_REQUEST);
?>
