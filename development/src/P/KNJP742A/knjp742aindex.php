<?php

require_once('for_php7.php');

require_once('knjp742aModel.inc');
require_once('knjp742aQuery.inc');

class knjp742aController extends Controller
{
    public $ModelClassName = "knjp742aModel";
    public $ProgramID      = "KNJP742A";

    public function main()
    {
        $sessionInstance =& Model::getModel();
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjp742aForm1");
                    }
                    break 2;
                case "":
                case "main":
                case "chgSchKind":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjp742aForm1");
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
$knjp742aCtl = new knjp742aController();
