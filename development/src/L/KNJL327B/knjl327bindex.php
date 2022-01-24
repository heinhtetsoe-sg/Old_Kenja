<?php

require_once('for_php7.php');

require_once('knjl327bModel.inc');
require_once('knjl327bQuery.inc');

class knjl327bController extends Controller {
    var $ModelClassName = "knjl327bModel";
    var $ProgramID      = "KNJL327B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327b":
                    $sessionInstance->knjl327bModel();
                    $this->callView("knjl327bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327bCtl = new knjl327bController;
?>
