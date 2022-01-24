<?php

require_once('for_php7.php');

require_once('knjl530fModel.inc');
require_once('knjl530fQuery.inc');

class knjl530fController extends Controller {
    var $ModelClassName = "knjl530fModel";
    var $ProgramID      = "KNJL530F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl530f":
                    $sessionInstance->knjl530fModel();
                    $this->callView("knjl530fForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl530f");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl530fCtl = new knjl530fController;
//var_dump($_REQUEST);
?>
