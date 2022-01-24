<?php

require_once('for_php7.php');

require_once('knjd214bModel.inc');
require_once('knjd214bQuery.inc');

class knjd214bController extends Controller {
    var $ModelClassName = "knjd214bModel";
    var $ProgramID      = "KNJD214B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjd214b");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjd214b");
                    break 1;
                case "":
                case "knjd214b":
                case "semechg":
                    $sessionInstance->knjd214bModel();
                    $this->callView("knjd214bForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd214bModel();
                    $this->callView("knjd214bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd214bCtl = new knjd214bController;
var_dump($_REQUEST);
?>
