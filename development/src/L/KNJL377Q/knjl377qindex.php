<?php

require_once('for_php7.php');

require_once('knjl377qModel.inc');
require_once('knjl377qQuery.inc');

class knjl377qController extends Controller {
    var $ModelClassName = "knjl377qModel";
    var $ProgramID      = "KNJL377Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "sat":
                case "sat_connect":
                case "birth":
                case "sex":
                case "sch":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    //$sessionInstance->getMainModel();
                    $this->callView("knjl377qForm1");
                    break 2;
                case "import":
                    $sessionInstance->getImportModel();
                    $this->callView("knjl377qForm1");
                    break 2;
                case "exec":
                    if(!$sessionInstance->getExecModel()){
                        $this->callView("knjl377qForm1");
                    }
                    break 2;
                case "wrkUpdate":
                    $sessionInstance->getWrkUpdateModel();
                    $this->callView("knjl377qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl377qForm1");
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
$knjl377qCtl = new knjl377qController;
?>
