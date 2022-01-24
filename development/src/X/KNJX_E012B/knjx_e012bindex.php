<?php

require_once('for_php7.php');

require_once('knjx_e012bModel.inc');
require_once('knjx_e012bQuery.inc');

class knjx_e012bController extends Controller
{
    public $ModelClassName = "knjx_e012bModel";
    public $ProgramID      = "KNJX_E012B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx_e012bForm1");
                    }
                    break 2;
                case "":
                case "sign":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_e012bForm1");
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
$knjx_e012bCtl = new knjx_e012bController();
