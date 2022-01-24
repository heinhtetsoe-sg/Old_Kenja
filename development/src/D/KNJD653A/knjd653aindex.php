<?php

require_once('for_php7.php');

require_once('knjd653aModel.inc');
require_once('knjd653aQuery.inc');

class knjd653aController extends Controller {
    var $ModelClassName = "knjd653aModel";
    var $ProgramID      = "KNJD653A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd653a":
                case "gakki":
                    $sessionInstance->knjd653aModel();
                    $this->callView("knjd653aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd653aCtl = new knjd653aController;
var_dump($_REQUEST);
?>
