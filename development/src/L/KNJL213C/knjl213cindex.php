<?php

require_once('for_php7.php');

require_once('knjl213cModel.inc');
require_once('knjl213cQuery.inc');

class knjl213cController extends Controller
{
    public $ModelClassName = "knjl213cModel";
    public $ProgramID      = "KNJL213C";

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
                        $this->callView("knjl213cForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl213cForm1");
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
$knjl213cCtl = new knjl213cController();
