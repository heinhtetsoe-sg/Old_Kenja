<?php

require_once('for_php7.php');

require_once('knjd183aModel.inc');
require_once('knjd183aQuery.inc');

class knjd183aController extends Controller {
    var $ModelClassName = "knjd183aModel";
    var $ProgramID      = "KNJD183A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd183a":
                case "gakki":
                    $sessionInstance->knjd183aModel();
                    $this->callView("knjd183aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd183aCtl = new knjd183aController;
var_dump($_REQUEST);
?>
