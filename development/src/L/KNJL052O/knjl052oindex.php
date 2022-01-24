<?php
require_once('knjl052oModel.inc');
require_once('knjl052oQuery.inc');

class knjl052oController extends Controller
{
    public $ModelClassName = "knjl052oModel";
    public $ProgramID      = "KNJL052O";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl052oForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl052oForm1");
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
$knjl052oCtl = new knjl052oController;
