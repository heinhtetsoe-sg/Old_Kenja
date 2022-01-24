<?php
require_once('knjl690iModel.inc');
require_once('knjl690iQuery.inc');

class knjl690iController extends Controller
{
    public $ModelClassName = "knjl690iModel";
    public $ProgramID      = "KNJL690I";

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
                case "head":
                case "error":
                case "data":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl690iForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl690iForm1");
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
$knjl690iCtl = new knjl690iController;
