<?php

require_once('for_php7.php');

require_once('knjl515aModel.inc');
require_once('knjl515aQuery.inc');

class knjl515aController extends Controller {
    var $ModelClassName = "knjl515aModel";
    var $ProgramID      = "KNJL515A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "kirikae":
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl515aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //$sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl515aCtl = new knjl515aController;
?>
