<?php

require_once('for_php7.php');

require_once('knjl070aModel.inc');
require_once('knjl070aQuery.inc');

class knjl070aController extends Controller
{
    public $ModelClassName = "knjl070aModel";
    public $ProgramID      = "KNJL070A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "csvInputMain":
                    $this->callView("knjl070aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("csvInputMain");
                    break 1;
                case "csvOutput":   //CSV出力
                    if (!$sessionInstance->getCsvModel()) {
                        $sessionInstance->setCmd("main");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl070aCtl = new knjl070aController();
