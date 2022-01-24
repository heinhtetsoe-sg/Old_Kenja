<?php
require_once('knjl107iModel.inc');
require_once('knjl107iQuery.inc');

class knjl107iController extends Controller
{
    public $ModelClassName = "knjl107iModel";
    public $ProgramID      = "KNJL107I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl107iForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl107iForm1");
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
$knjl107iCtl = new knjl107iController;
