<?php

require_once('for_php7.php');

require_once('knjc030aModel.inc');
require_once('knjc030aQuery.inc');

class knjc030aController extends Controller
{
    public $ModelClassName = "knjc030aModel";
    public $ProgramID        = "KNJC030A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjc030aForm1");
                    break 2;
                case "clear":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
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
$knjc030aCtl = new knjc030aController();
//var_dump($_REQUEST);
