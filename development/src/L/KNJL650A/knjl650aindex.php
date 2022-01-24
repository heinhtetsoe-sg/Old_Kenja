<?php

require_once('for_php7.php');

require_once('knjl650aModel.inc');
require_once('knjl650aQuery.inc');

class knjl650aController extends Controller
{
    public $ModelClassName = "knjl650aModel";
    public $ProgramID      = "KNJL650A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                case "back":
                case "next":
                case "end":
                    $this->callView("knjl650aForm1");
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
                        $this->callView("knjl650aForm1");
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
$knjl650aCtl = new knjl650aController();
