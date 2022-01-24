<?php
require_once('knjl632fModel.inc');
require_once('knjl632fQuery.inc');

class knjl632fController extends Controller
{
    public $ModelClassName = "knjl632fModel";
    public $ProgramID      = "KNJL632F";

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
                // case "csv":
                case "head":
                case "error":
                // case "data":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl632fForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl632fForm1");
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
$knjl632fCtl = new knjl632fController;
?>
