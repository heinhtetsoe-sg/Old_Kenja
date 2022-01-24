<?php

require_once('for_php7.php');

require_once('knjl013pModel.inc');
require_once('knjl013pQuery.inc');

class knjl013pController extends Controller {
    var $ModelClassName = "knjl013pModel";
    var $ProgramID      = "KNJL013P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl013p":
                    $sessionInstance->knjl013pModel();
                    $this->callView("knjl013pForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl013p");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl013pCtl = new knjl013pController;
//var_dump($_REQUEST);
?>
