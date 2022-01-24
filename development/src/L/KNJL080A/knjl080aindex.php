<?php

require_once('for_php7.php');

require_once('knjl080aModel.inc');
require_once('knjl080aQuery.inc');

class knjl080aController extends Controller
{
    public $ModelClassName = "knjl080aModel";
    public $ProgramID      = "KNJL080A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl080aForm1");
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
$knjl080aCtl = new knjl080aController();
