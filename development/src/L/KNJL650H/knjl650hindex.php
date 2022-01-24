<?php
require_once('knjl650hModel.inc');
require_once('knjl650hQuery.inc');

class knjl650hController extends Controller
{
    public $ModelClassName = "knjl650hModel";
    public $ProgramID      = "KNJL650H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                case "end":
                case "csvInputMain":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl650hForm1");
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
                        $this->callView("knjl650hForm1");
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
$knjl650hCtl = new knjl650hController;
?>
