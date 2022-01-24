<?php

require_once('for_php7.php');

require_once('knjl550aModel.inc');
require_once('knjl550aQuery.inc');

class knjl550aController extends Controller
{
    public $ModelClassName = "knjl550aModel";
    public $ProgramID      = "KNJL550A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "search":
                case "reload":
                case "back":
                case "next":
                case "ajaxGetName":
                    $this->callView("knjl550aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("search");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("search");
                    break 1;
                case "csvOutput":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl550aForm1");
                    }
                    break 2;
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
$knjl550aCtl = new knjl550aController;
