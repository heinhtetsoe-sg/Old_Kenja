<?php

require_once('for_php7.php');

require_once('knjl327rModel.inc');
require_once('knjl327rQuery.inc');

class knjl327rController extends Controller {
    var $ModelClassName = "knjl327rModel";
    var $ProgramID      = "KNJL327R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327r":
                    $sessionInstance->knjl327rModel();
                    $this->callView("knjl327rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327rCtl = new knjl327rController;
?>
