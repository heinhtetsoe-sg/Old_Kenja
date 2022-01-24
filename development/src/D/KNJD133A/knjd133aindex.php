<?php

require_once('for_php7.php');

require_once('knjd133aModel.inc');
require_once('knjd133aQuery.inc');

class knjd133aController extends Controller {
    var $ModelClassName = "knjd133aModel";
    var $ProgramID      = "KNJD133A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjd133a");
                    break 1;
                case "":
                case "knjd133a":
                case "semechg":
                    $sessionInstance->knjd133aModel();
                    $this->callView("knjd133aForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd133aModel();
                    $this->callView("knjd133aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd133aCtl = new knjd133aController;
//var_dump($_REQUEST);
?>
