<?php

require_once('for_php7.php');

require_once('knjd062bModel.inc');
require_once('knjd062bQuery.inc');

class knjd062bController extends Controller {
    var $ModelClassName = "knjd062bModel";
    var $ProgramID      = "KNJD062B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd062b":
                case "semechg":
                    $sessionInstance->knjd062bModel();
                    $this->callView("knjd062bForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd062bModel();
                    $this->callView("knjd062bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd062bCtl = new knjd062bController;
var_dump($_REQUEST);
?>
