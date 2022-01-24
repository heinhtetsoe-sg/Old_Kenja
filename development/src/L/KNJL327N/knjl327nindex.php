<?php

require_once('for_php7.php');

require_once('knjl327nModel.inc');
require_once('knjl327nQuery.inc');

class knjl327nController extends Controller {
    var $ModelClassName = "knjl327nModel";
    var $ProgramID      = "KNJL327N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327n":
                    $sessionInstance->knjl327nModel();
                    $this->callView("knjl327nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327nCtl = new knjl327nController;
?>
