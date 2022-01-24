<?php

require_once('for_php7.php');

require_once('knjl327sModel.inc');
require_once('knjl327sQuery.inc');

class knjl327sController extends Controller {
    var $ModelClassName = "knjl327sModel";
    var $ProgramID      = "KNJL327S";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327s":
                    $sessionInstance->knjl327sModel();
                    $this->callView("knjl327sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327sCtl = new knjl327sController;
?>
