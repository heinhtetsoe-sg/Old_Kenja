<?php

require_once('for_php7.php');

require_once('knjl328tModel.inc');
require_once('knjl328tQuery.inc');

class knjl328tController extends Controller {
    var $ModelClassName = "knjl328tModel";
    var $ProgramID      = "KNJL328T";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328t":
                    $sessionInstance->knjl328tModel();
                    $this->callView("knjl328tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl328tCtl = new knjl328tController;
?>
