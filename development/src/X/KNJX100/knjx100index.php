<?php

require_once('for_php7.php');

require_once('knjx100Model.inc');
require_once('knjx100Query.inc');

class knjx100Controller extends Controller
{
    public $ModelClassName = "knjx100Model";
    public $ProgramID      = "KNJX100";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取り込み
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":   //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx100Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjx100Form1");
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
$knjx100Ctl = new knjx100Controller();
