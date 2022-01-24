<?php

require_once('for_php7.php');

require_once('knjl328sModel.inc');
require_once('knjl328sQuery.inc');

class knjl328sController extends Controller {
    var $ModelClassName = "knjl328sModel";
    var $ProgramID      = "KNJL328S";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328s":
                    $sessionInstance->knjl328sModel();
                    $this->callView("knjl328sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl328sCtl = new knjl328sController;
?>
