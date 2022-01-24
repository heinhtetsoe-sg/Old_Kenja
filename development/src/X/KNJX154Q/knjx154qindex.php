<?php

require_once('for_php7.php');

require_once('knjx154qModel.inc');
require_once('knjx154qQuery.inc');

class knjx154qController extends Controller
{
    public $ModelClassName = "knjx154qModel";
    public $ProgramID      = "KNJX154Q";

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
                        $this->callView("knjx154qForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx154qForm1");
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
$knjx154qCtl = new knjx154qController();
