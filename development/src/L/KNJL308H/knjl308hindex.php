<?php

require_once('for_php7.php');

require_once('knjl308hModel.inc');
require_once('knjl308hQuery.inc');

class knjl308hController extends Controller {
    var $ModelClassName = "knjl308hModel";
    var $ProgramID      = "KNJL308H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl308h":
                    $this->callView("knjl308hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl308hCtl = new knjl308hController;
//var_dump($_REQUEST);
?>
