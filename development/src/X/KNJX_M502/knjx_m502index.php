<?php

require_once('for_php7.php');

require_once('knjx_m502Model.inc');
require_once('knjx_m502Query.inc');

class knjx_m502Controller extends Controller
{
    public $ModelClassName = "knjx_m502Model";
    public $ProgramID      = "KNJX_M502";

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
                        $this->callView("knjx_m502Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_m502Form1");
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
$knjx_m502Ctl = new knjx_m502Controller();
