<?php

require_once('for_php7.php');

require_once('knjl051nModel.inc');
require_once('knjl051nQuery.inc');

class knjl051nController extends Controller
{
    public $ModelClassName = "knjl051nModel";
    public $ProgramID      = "KNJL051N";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl051nForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl051nForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl051nCtl = new knjl051nController();
