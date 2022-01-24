<?php

require_once('for_php7.php');

require_once('knjf140jModel.inc');
require_once('knjf140jQuery.inc');

class knjf140jController extends Controller
{
    public $ModelClassName = "knjf140jModel";
    public $ProgramID      = "KNJF140J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取り込み
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":         //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf140jForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf140jForm1");
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
$knjf140jCtl = new knjf140jController();
