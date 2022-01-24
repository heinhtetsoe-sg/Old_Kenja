<?php

require_once('for_php7.php');

require_once('knjl330aModel.inc');
require_once('knjl330aQuery.inc');

class knjl330aController extends Controller
{
    public $ModelClassName = "knjl330aModel";
    public $ProgramID      = "KNJL330A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl330a":
                    $this->callView("knjl330aForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl330aForm1");
                    }
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
$knjl330aCtl = new knjl330aController();
