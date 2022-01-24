<?php

require_once('for_php7.php');

require_once('knjl114dModel.inc');
require_once('knjl114dQuery.inc');

class knjl114dController extends Controller
{
    public $ModelClassName = "knjl114dModel";
    public $ProgramID      = "KNJL114D";

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
                        $this->callView("knjl114dForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl114dForm1");
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
$knjl114dCtl = new knjl114dController();
