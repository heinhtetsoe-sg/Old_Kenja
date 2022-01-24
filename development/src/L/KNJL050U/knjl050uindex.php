<?php

require_once('for_php7.php');

require_once('knjl050uModel.inc');
require_once('knjl050uQuery.inc');

class knjl050uController extends Controller
{
    public $ModelClassName = "knjl050uModel";
    public $ProgramID      = "KNJL050U";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "next":
                case "back":
                case "testsub":
                case "hall":
                case "reset":
                case "end":
                    $this->callView("knjl050uForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("reset");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("reset");
                    break 1;
                case "csvOutput":   //CSV出力
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl050uForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
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
$knjl050uCtl = new knjl050uController();
