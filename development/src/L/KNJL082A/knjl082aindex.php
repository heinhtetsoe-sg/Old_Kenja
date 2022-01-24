<?php

require_once('for_php7.php');

require_once('knjl082aModel.inc');
require_once('knjl082aQuery.inc');

class knjl082aController extends Controller
{
    public $ModelClassName = "knjl082aModel";
    public $ProgramID      = "KNJL082A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl082aForm1");
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
$knjl082aCtl = new knjl082aController();
