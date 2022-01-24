<?php
require_once('knjl017fModel.inc');
require_once('knjl017fQuery.inc');

class knjl017fController extends Controller
{
    public $ModelClassName = "knjl017fModel";
    public $ProgramID      = "KNJL017F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl017fForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl017fForm1");
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
$knjl017fCtl = new knjl017fController();
