<?php
require_once('knjl520iModel.inc');
require_once('knjl520iQuery.inc');

class knjl520iController extends Controller
{
    public $ModelClassName = "knjl520iModel";
    public $ProgramID      = "KNJL520I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                case "csvInputMain":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl520iForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("csvInputMain");
                    break 1;
                case "csvOutput":
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl520iForm1");
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
$knjl520iCtl = new knjl520iController;
