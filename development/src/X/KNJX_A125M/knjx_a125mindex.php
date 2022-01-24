<?php

require_once('for_php7.php');

require_once('knjx_a125mModel.inc');
require_once('knjx_a125mQuery.inc');

class knjx_a125mController extends Controller
{
    public $ModelClassName = "knjx_a125mModel";
    public $ProgramID      = "KNJX_A125M";

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
                        $this->callView("knjx_a125mForm1");
                    }
                    break 2;
                case "":
                case "sign":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_a125mForm1");
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
$knjx_a125mCtl = new knjx_a125mController();
