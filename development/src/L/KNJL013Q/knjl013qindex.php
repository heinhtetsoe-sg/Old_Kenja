<?php

require_once('for_php7.php');

require_once('knjl013qModel.inc');
require_once('knjl013qQuery.inc');

class knjl013qController extends Controller {
    var $ModelClassName = "knjl013qModel";
    var $ProgramID      = "KNJL013Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl013q":
                    $sessionInstance->knjl013qModel();
                    $this->callView("knjl013qForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl013q");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl013qCtl = new knjl013qController;
//var_dump($_REQUEST);
?>
