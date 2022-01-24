<?php

require_once('for_php7.php');

require_once('knjl081aModel.inc');
require_once('knjl081aQuery.inc');

class knjl081aController extends Controller
{
    public $ModelClassName = "knjl081aModel";
    public $ProgramID      = "KNJL081A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl081aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
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
$knjl081aCtl = new knjl081aController();
