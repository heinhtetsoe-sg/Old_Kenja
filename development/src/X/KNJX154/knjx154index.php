<?php

require_once('for_php7.php');

require_once('knjx154Model.inc');
require_once('knjx154Query.inc');

class knjx154Controller extends Controller
{
    public $ModelClassName = "knjx154Model";
    public $ProgramID      = "KNJX154";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":         //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx154Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx154Form1");
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
$knjx154Ctl = new knjx154Controller();
