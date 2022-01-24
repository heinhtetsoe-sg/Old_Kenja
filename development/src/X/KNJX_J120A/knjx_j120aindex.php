<?php

require_once('for_php7.php');

require_once('knjx_j120aModel.inc');
require_once('knjx_j120aQuery.inc');

class knjx_j120aController extends Controller
{
    public $ModelClassName = "knjx_j120aModel";
    public $ProgramID      = "KNJX_J120A";

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
                        $this->callView("knjx_j120aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->knjx_j120aModel();
                    $this->callView("knjx_j120aForm1");
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
$knjx_j120aCtl = new knjx_j120aController();
