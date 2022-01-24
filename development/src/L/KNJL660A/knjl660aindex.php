<?php

require_once('for_php7.php');

require_once('knjl660aModel.inc');
require_once('knjl660aQuery.inc');

class knjl660aController extends Controller
{
    public $ModelClassName = "knjl660aModel";
    public $ProgramID      = "KNJL660A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl660aForm1");
                    break 2;
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
$knjl660aCtl = new knjl660aController();
