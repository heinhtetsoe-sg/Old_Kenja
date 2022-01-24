<?php

require_once('for_php7.php');

require_once('knjl326rModel.inc');
require_once('knjl326rQuery.inc');

class knjl326rController extends Controller {
    var $ModelClassName = "knjl326rModel";
    var $ProgramID      = "KNJL326R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl326r":
                    $sessionInstance->knjl326rModel();
                    $this->callView("knjl326rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl326rCtl = new knjl326rController;
?>
