<?php

require_once('for_php7.php');

require_once('knjl310qModel.inc');
require_once('knjl310qQuery.inc');

class knjl310qController extends Controller
{
    public $ModelClassName = "knjl310qModel";
    public $ProgramID      = "KNJL310Q";

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
                        $this->callView("knjl310qForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl310qForm1");
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
$knjl310qCtl = new knjl310qController();
