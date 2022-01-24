<?php

require_once('for_php7.php');

require_once('knjl780hModel.inc');
require_once('knjl780hQuery.inc');

class knjl780hController extends Controller
{
    public $ModelClassName = "knjl780hModel";
    public $ProgramID      = "KNJL780H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csvInputMain":
                case "main":
                case "read":
                case "reset":
                case "end":
                    $this->callView("knjl780hForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("csvInputMain");
                    break 1;
                case "csvOutput":
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl580hForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
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
$knjl780hCtl = new knjl780hController();
