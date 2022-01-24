<?php

require_once('for_php7.php');

require_once('knjd653gModel.inc');
require_once('knjd653gQuery.inc');

class knjd653gController extends Controller {
    var $ModelClassName = "knjd653gModel";
    var $ProgramID      = "KNJD653G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd653g":
                case "gakki":
                    $sessionInstance->knjd653gModel();
                    $this->callView("knjd653gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd653gCtl = new knjd653gController;
var_dump($_REQUEST);
?>
