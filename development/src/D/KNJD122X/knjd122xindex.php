<?php

require_once('for_php7.php');

require_once('knjd122xModel.inc');
require_once('knjd122xQuery.inc');

class knjd122xController extends Controller
{
    public $ModelClassName = "knjd122xModel";
    public $ProgramID      = "KNJD122X";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd122xForm1");
                    }
                    break 2;
                case "":
                case "main":
                case "read":
                    $sessionInstance->getMainModel();
                    $this->callView("knjd122xForm1");
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
$knjd122xCtl = new knjd122xController();
