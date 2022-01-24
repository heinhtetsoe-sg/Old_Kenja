<?php

require_once('for_php7.php');

require_once('knjd655bModel.inc');
require_once('knjd655bQuery.inc');

class knjd655bController extends Controller {
    var $ModelClassName = "knjd655bModel";
    var $ProgramID      = "KNJD655B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd655b":
                case "gakki":
                    $sessionInstance->knjd655bModel();
                    $this->callView("knjd655bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd655bCtl = new knjd655bController;
var_dump($_REQUEST);
?>
