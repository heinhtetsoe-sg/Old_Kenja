<?php

require_once('for_php7.php');

require_once('knjd653bModel.inc');
require_once('knjd653bQuery.inc');

class knjd653bController extends Controller {
    var $ModelClassName = "knjd653bModel";
    var $ProgramID      = "KNJD653B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd653b":
                case "gakki":
                    $sessionInstance->knjd653bModel();
                    $this->callView("knjd653bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd653bCtl = new knjd653bController;
var_dump($_REQUEST);
?>
