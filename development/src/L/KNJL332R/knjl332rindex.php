<?php

require_once('for_php7.php');

require_once('knjl332rModel.inc');
require_once('knjl332rQuery.inc');

class knjl332rController extends Controller {
    var $ModelClassName = "knjl332rModel";
    var $ProgramID      = "KNJL332R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl332r":
                    $sessionInstance->knjl332rModel();
                    $this->callView("knjl332rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl332rCtl = new knjl332rController;
var_dump($_REQUEST);
?>
