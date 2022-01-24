<?php

require_once('for_php7.php');

require_once('knjl082rModel.inc');
require_once('knjl082rQuery.inc');

class knjl082rController extends Controller
{
    public $ModelClassName = "knjl082rModel";
    public $ProgramID      = "KNJL082R";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl082rForm1");
                    }
                    break 2;
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjl082rForm1");
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
$knjl082rCtl = new knjl082rController();
