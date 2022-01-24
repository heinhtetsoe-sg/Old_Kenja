<?php

require_once('for_php7.php');

require_once('knjl423yModel.inc');
require_once('knjl423yQuery.inc');

class knjl423yController extends Controller {
    var $ModelClassName = "knjl423yModel";
    var $ProgramID      = "KNJL423Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl423y":
                    $sessionInstance->knjl423yModel();
                    $this->callView("knjl423yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl423yCtl = new knjl423yController;
?>
