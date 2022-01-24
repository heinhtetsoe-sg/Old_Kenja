<?php

require_once('for_php7.php');

require_once('knjl327tModel.inc');
require_once('knjl327tQuery.inc');

class knjl327tController extends Controller {
    var $ModelClassName = "knjl327tModel";
    var $ProgramID      = "KNJL327T";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327t":
                    $sessionInstance->knjl327tModel();
                    $this->callView("knjl327tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327tCtl = new knjl327tController;
?>
