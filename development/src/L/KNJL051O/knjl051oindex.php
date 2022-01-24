<?php
require_once('knjl051oModel.inc');
require_once('knjl051oQuery.inc');

class knjl051oController extends Controller
{
    public $ModelClassName = "knjl051oModel";
    public $ProgramID      = "KNJL051O";     //プログラムID

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
                    $this->callView("knjl051oForm1");
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
                        $this->callView("knjl580hForm1");
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
$knjl051oCtl = new knjl051oController;
?>
