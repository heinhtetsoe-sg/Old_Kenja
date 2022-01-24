<?php

require_once('for_php7.php');

require_once('knjl326bModel.inc');
require_once('knjl326bQuery.inc');

class knjl326bController extends Controller
{
    public $ModelClassName = "knjl326bModel";
    public $ProgramID      = "KNJL326B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl326b":
                    $sessionInstance->knjl326bModel();
                    $this->callView("knjl326bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl326bCtl = new knjl326bController();
