<?php

require_once('for_php7.php');

require_once('knjl629fModel.inc');
require_once('knjl629fQuery.inc');

class knjl629fController extends Controller {
    var $ModelClassName = "knjl629fModel";
    var $ProgramID      = "KNJL629F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl629fForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl629fCtl = new knjl629fController;
?>
