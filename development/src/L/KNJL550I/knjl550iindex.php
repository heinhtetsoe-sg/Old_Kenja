<?php
require_once('knjl550iModel.inc');
require_once('knjl550iQuery.inc');

class knjl550iController extends Controller
{
    public $ModelClassName = "knjl550iModel";
    public $ProgramID      = "KNJL550I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "updread":
                case "reset":
                case "end":
                case "csvInputMain":
                    $this->callView("knjl550iForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updread");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("csvInputMain");
                    break 1;
                case "csvOutput":
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl550iForm1");
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
$knjl550iCtl = new knjl550iController;
