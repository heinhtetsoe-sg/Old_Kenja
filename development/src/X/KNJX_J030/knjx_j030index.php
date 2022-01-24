<?php

require_once('for_php7.php');

require_once('knjx_j030Model.inc');
require_once('knjx_j030Query.inc');

class knjx_j030Controller extends Controller
{
    public $ModelClassName = "knjx_j030Model";
    public $ProgramID      = "KNJX_J030";

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
                        $this->callView("knjx_j030Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->knjx_j030Model();
                    $this->callView("knjx_j030Form1");
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
$knjx_j030Ctl = new knjx_j030Controller();
