<?php

require_once('for_php7.php');

require_once('knjl672aModel.inc');
require_once('knjl672aQuery.inc');

class knjl672aController extends Controller
{
    public $ModelClassName = "knjl672aModel";
    public $ProgramID      = "KNJL672A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "clear":
                    $sessionInstance->knjl672aModel();
                    $this->callView("knjl672aForm1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;

                case "csvInput":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "csvOutput":   //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getCsvOutputModel()) {
                        $this->callView("knjl672aForm1");
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
$knjl672aCtl = new knjl672aController();
