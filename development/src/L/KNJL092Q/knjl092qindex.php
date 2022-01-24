<?php

require_once('for_php7.php');

require_once('knjl092qModel.inc');
require_once('knjl092qQuery.inc');

class knjl092qController extends Controller
{
    public $ModelClassName = "knjl092qModel";
    public $ProgramID      = "KNJL092Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl092qForm1");
                    break 2;
                case "numbering":
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
$knjl092qCtl = new knjl092qController();
