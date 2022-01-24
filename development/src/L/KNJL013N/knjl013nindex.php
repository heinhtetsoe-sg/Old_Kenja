<?php

require_once('for_php7.php');

require_once('knjl013nModel.inc');
require_once('knjl013nQuery.inc');

class knjl013nController extends Controller {
    var $ModelClassName = "knjl013nModel";
    var $ProgramID      = "KNJL013N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl013n":
                    $sessionInstance->knjl013nModel();
                    $this->callView("knjl013nForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl013n");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl013nCtl = new knjl013nController;
//var_dump($_REQUEST);
?>
