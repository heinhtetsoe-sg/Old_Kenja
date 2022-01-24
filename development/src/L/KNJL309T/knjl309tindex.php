<?php

require_once('for_php7.php');

require_once('knjl309tModel.inc');
require_once('knjl309tQuery.inc');

class knjl309tController extends Controller {
    var $ModelClassName = "knjl309tModel";
    var $ProgramID      = "KNJL309T";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl309t":
                    $sessionInstance->knjl309tModel();
                    $this->callView("knjl309tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl309tCtl = new knjl309tController;
?>
