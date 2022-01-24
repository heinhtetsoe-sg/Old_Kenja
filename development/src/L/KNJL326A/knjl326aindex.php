<?php

require_once('for_php7.php');

require_once('knjl326aModel.inc');
require_once('knjl326aQuery.inc');

class knjl326aController extends Controller
{
    public $ModelClassName = "knjl326aModel";
    public $ProgramID      = "KNJL326A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl326a":
                case "change":
                case "read":
                case "print":
                    $this->callView("knjl326aForm1");
                    break 2;
                case "updateAndPrint":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("print");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl326aCtl = new knjl326aController();
