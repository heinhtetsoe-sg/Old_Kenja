<?php

require_once('for_php7.php');

require_once('knjl621fModel.inc');
require_once('knjl621fQuery.inc');

class knjl621fController extends Controller {
    var $ModelClassName = "knjl621fModel";
    var $ProgramID      = "KNJL621F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl621f":
                    $sessionInstance->knjl621fModel();
                    $this->callView("knjl621fForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl621fForm1");
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
$knjl621fCtl = new knjl621fController;
//var_dump($_REQUEST);
?>
