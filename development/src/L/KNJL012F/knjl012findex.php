<?php

require_once('for_php7.php');

require_once('knjl012fModel.inc');
require_once('knjl012fQuery.inc');

class knjl012fController extends Controller {
    var $ModelClassName = "knjl012fModel";
    var $ProgramID      = "KNJL012F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl012f":
                    $sessionInstance->knjl012fModel();
                    $this->callView("knjl012fForm1");
                    exit;
                case "exec":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl012f");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl012fCtl = new knjl012fController;
?>
