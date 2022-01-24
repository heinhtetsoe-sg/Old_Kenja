<?php

require_once('for_php7.php');

require_once('knjl016pModel.inc');
require_once('knjl016pQuery.inc');

class knjl016pController extends Controller
{
    public $ModelClassName = "knjl016pModel";
    public $ProgramID      = "KNJL016P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getHeadDownloadModel()) {
                        $this->callView("knjl016pForm1");
                    }
                    break 2;
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
                        $this->callView("knjl016pForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl016pForm1");
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
$knjl016pCtl = new knjl016pController();
