<?php

require_once('for_php7.php');

require_once('knjl428yModel.inc');
require_once('knjl428yQuery.inc');

class knjl428yController extends Controller {
    var $ModelClassName = "knjl428yModel";
    var $ProgramID      = "KNJL428Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl428y":
                case "change":
                    $sessionInstance->knjl428yModel();
                    $this->callView("knjl428yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl428yCtl = new knjl428yController;
?>
