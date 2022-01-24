<?php

require_once('for_php7.php');

require_once('knjl050gModel.inc');
require_once('knjl050gQuery.inc');

class knjl050gController extends Controller
{
    public $ModelClassName = "knjl050gModel";
    public $ProgramID      = "KNJL050G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "hall":
                case "test":
                case "reset":
                case "end":
                    $this->callView("knjl050gForm1");
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
                        $this->callView("knjl050gForm1");
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
$knjl050gCtl = new knjl050gController();
