<?php

require_once('for_php7.php');

require_once('knjx180bModel.inc');
require_once('knjx180bQuery.inc');

class knjx180bController extends Controller
{
    public $ModelClassName = "knjx180bModel";
    public $ProgramID      = "KNJX180B";

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
                        $this->callView("knjx180bForm1");
                    }
                    break 2;
                case "":
                case "sign":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx180bForm1");
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
$knjx180bCtl = new knjx180bController();
