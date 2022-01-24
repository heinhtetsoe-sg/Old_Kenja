<?php

require_once('for_php7.php');

require_once('knjf150dModel.inc');
require_once('knjf150dQuery.inc');

class knjf150dController extends Controller {
    var $ModelClassName = "knjf150dModel";
    var $ProgramID        = "KNJF150D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "year":
                    $this->callView("knjf150dForm1");
                   break 2;

                case "clear":
                    $sessionInstance->setCmd("main");
                    break 1;

                case "confirm":
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;

                case "error":
                    $this->callView("error");
                    break 2;

                case "read":
                    $sessionInstance->keyClean("");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "read_before":
                    $sessionInstance->key_Move_Model("before");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "read_next":
                    $sessionInstance->key_Move_Model("next");
                    $sessionInstance->setCmd("main");
                    break 1;

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
$knjf150dCtl = new knjf150dController;
//var_dump($_REQUEST);
?>
