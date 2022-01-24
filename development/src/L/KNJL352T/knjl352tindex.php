<?php

require_once('for_php7.php');

require_once('knjl352tModel.inc');
require_once('knjl352tQuery.inc');

class knjl352tController extends Controller {
    var $ModelClassName = "knjl352tModel";
    var $ProgramID      = "KNJL352T";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl352t":
                    $sessionInstance->knjl352tModel();
                    $this->callView("knjl352tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl352tCtl = new knjl352tController;
?>
