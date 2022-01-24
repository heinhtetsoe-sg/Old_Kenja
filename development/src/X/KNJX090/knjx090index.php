<?php

require_once('for_php7.php');

require_once('knjx090Model.inc');
require_once('knjx090Query.inc');

class knjx090Controller extends Controller
{
    public $ModelClassName = "knjx090Model";
    public $ProgramID      = "KNJX090";

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
                        $this->callView("knjx090Form1");
                    }
                    break 2;
                case "csv2": // 自動生成
                    $sessionInstance->setAccessLogDetail("IEO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel2()) {
                        $this->callView("knjx090Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjx090Form1");
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
$knjx090Ctl = new knjx090Controller();
