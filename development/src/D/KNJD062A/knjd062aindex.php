<?php

require_once('for_php7.php');

require_once('knjd062aModel.inc');
require_once('knjd062aQuery.inc');

class knjd062aController extends Controller {
    var $ModelClassName = "knjd062aModel";
    var $ProgramID      = "KNJD062A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd062a":
                case "semechg":
                    $sessionInstance->knjd062aModel();
                    $this->callView("knjd062aForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd062aModel();
                    $this->callView("knjd062aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd062aCtl = new knjd062aController;
var_dump($_REQUEST);
?>
