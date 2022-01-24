<?php

require_once('for_php7.php');

require_once('knjl384jModel.inc');
require_once('knjl384jQuery.inc');

class knjl384jController extends Controller
{
    public $ModelClassName = "knjl384jModel";
    public $ProgramID      = "KNJL384J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取り込み
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":   //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl384jForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl384jForm1");
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
$knjl384jCtl = new knjl384jController();
