<?php

require_once('for_php7.php');

require_once('knje372cModel.inc');
require_once('knje372cQuery.inc');

class knje372cController extends Controller {
    var $ModelClassName = "knje372cModel";
    var $ProgramID      = "KNJE372C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeCmb":
                case "reset":
                    $this->callView("knje372cForm1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje372cCtl = new knje372cController;
//var_dump($_REQUEST);
?>
