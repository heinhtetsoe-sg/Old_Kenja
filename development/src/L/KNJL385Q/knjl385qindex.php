<?php

require_once('for_php7.php');

require_once('knjl385qModel.inc');
require_once('knjl385qQuery.inc');

class knjl385qController extends Controller {
    var $ModelClassName = "knjl385qModel";
    var $ProgramID      = "KNJl385q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "nendoChange":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    //$sessionInstance->getMainModel();
                    $this->callView("knjl385qForm1");
                    break 2;
                case "insert":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->InsertModel();
                    $sessionInstance->cmd = "";
                    //$this->callView("knjl385qForm1");
                    break 1;
                case "update":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->UpdateModel();
                    $sessionInstance->cmd = "";
                    //$this->callView("knjl385qForm1");
                    break 1;
                case "delete":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->DeleteModel();
                    $sessionInstance->cmd = "";
                    //$this->callView("knjl385qForm1");
                    break 1;
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
$knjl385qCtl = new knjl385qController;
?>
