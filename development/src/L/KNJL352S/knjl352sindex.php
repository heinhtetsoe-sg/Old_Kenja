<?php

require_once('for_php7.php');

require_once('knjl352sModel.inc');
require_once('knjl352sQuery.inc');

class knjl352sController extends Controller {
    var $ModelClassName = "knjl352sModel";
    var $ProgramID      = "KNJL352S";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl352s":
                    $sessionInstance->knjl352sModel();
                    $this->callView("knjl352sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl352sCtl = new knjl352sController;
?>
