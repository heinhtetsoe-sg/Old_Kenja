<?php

require_once('for_php7.php');

require_once('knjc020aModel.inc');
require_once('knjc020aQuery.inc');

class knjc020aController extends Controller
{
    public $ModelClassName = "knjc020aModel";
    public $ProgramID        = "KNJC020A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjc020aForm1");
                    break 2;

                case "clear":
                    $sessionInstance->setCmd("main");
                    break 1;

                case "confirm":
                case "update":
                case "update2":
                    if (trim($sessionInstance->cmd) == "update2") {
                        $sessionInstance->setCmd("update");
                    }
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
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
                    $sessionInstance->keyMoveModel("before");
                    $sessionInstance->setCmd("main");
                    break 1;

                case "read_next":
                    $sessionInstance->keyMoveModel("next");
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
$knjc020aCtl = new knjc020aController();
//var_dump($_REQUEST);
