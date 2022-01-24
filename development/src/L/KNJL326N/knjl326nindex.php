<?php

require_once('for_php7.php');

require_once('knjl326nModel.inc');
require_once('knjl326nQuery.inc');

class knjl326nController extends Controller {
    var $ModelClassName = "knjl326nModel";
    var $ProgramID      = "KNJL326N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl326n":
                    $sessionInstance->knjl326nModel();
                    $this->callView("knjl326nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl326nCtl = new knjl326nController;
?>
