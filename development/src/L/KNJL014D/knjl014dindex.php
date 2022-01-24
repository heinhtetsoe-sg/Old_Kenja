<?php

require_once('for_php7.php');

require_once('knjl014dModel.inc');
require_once('knjl014dQuery.inc');

class knjl014dController extends Controller
{
    public $ModelClassName = "knjl014dModel";
    public $ProgramID      = "KNJL014D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //CSV出力
                case "csv":
                case "head":
                case "error":
                case "data":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl014dForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl014dForm1");
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
$knjl014dCtl = new knjl014dController();
