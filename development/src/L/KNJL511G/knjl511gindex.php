<?php

require_once('for_php7.php');

require_once('knjl511gModel.inc');
require_once('knjl511gQuery.inc');

class knjl511gController extends Controller {
    var $ModelClassName = "knjl511gModel";
    var $ProgramID      = "KNJL511G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl511g":
                    $sessionInstance->knjl511gModel();
                    $this->callView("knjl511gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl511gCtl = new knjl511gController;
?>
