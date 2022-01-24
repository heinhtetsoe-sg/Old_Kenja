<?php

require_once('for_php7.php');

require_once('knjc201_absence_listModel.inc');
require_once('knjc201_absence_listQuery.inc');

class knjc201_absence_listController extends Controller
{
    public $ModelClassName = "knjc201_absence_listModel";
    public $ProgramID      = "KNJC201_ABSENCE_LIST";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjc201_absence_listForm1");
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
                case "changeDate":
                    $sessionInstance->keyMoveModel("changeDate");
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
$knjc201_absence_listCtl = new knjc201_absence_listController();
