<?php

require_once('for_php7.php');

require_once('knjl300nModel.inc');
require_once('knjl300nQuery.inc');

class knjl300nController extends Controller {
    var $ModelClassName = "knjl300nModel";
    var $ProgramID      = "KNJL300N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300n":
                    $sessionInstance->knjl300nModel();
                    $this->callView("knjl300nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl300nCtl = new knjl300nController;
//var_dump($_REQUEST);
?>
