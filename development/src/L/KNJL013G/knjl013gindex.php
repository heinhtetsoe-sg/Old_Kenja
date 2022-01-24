<?php

require_once('for_php7.php');

require_once('knjl013gModel.inc');
require_once('knjl013gQuery.inc');

class knjl013gController extends Controller {
    var $ModelClassName = "knjl013gModel";
    var $ProgramID      = "KNJL013G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl013g":
                    $sessionInstance->knjl013gModel();
                    $this->callView("knjl013gForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl013g");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl013gCtl = new knjl013gController;
//var_dump($_REQUEST);
?>
