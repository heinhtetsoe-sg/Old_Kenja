<?php

require_once('for_php7.php');

require_once('knjl433hModel.inc');
require_once('knjl433hQuery.inc');

class knjl433hController extends Controller
{
    public $ModelClassName = "knjl433hModel";
    public $ProgramID      = "KNJL433H";

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
                    $this->callView("knjl433hForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "exec":
                    if (!$sessionInstance->performCSV()) {
                        $this->callView("knjl433hForm1");
                    }
                    break 1;

                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl433hCtl = new knjl433hController();
