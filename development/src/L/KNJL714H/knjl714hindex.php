<?php
require_once('knjl714hModel.inc');
require_once('knjl714hQuery.inc');

class knjl714hController extends Controller
{
    public $ModelClassName = "knjl714hModel";
    public $ProgramID      = "KNJL714H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //データ生成(mirai → 賢者)
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
                        $this->callView("knjl714hForm1");
                    }
                    break 2;
                case "":
                case "main":
                case "chgAppAppDiv":
                case "chgTestDiv":
                            $this->callView("knjl714hForm1");
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
$knjl714hCtl = new knjl714hController;
