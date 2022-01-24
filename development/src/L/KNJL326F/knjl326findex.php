<?php

require_once('for_php7.php');

require_once('knjl326fModel.inc');
require_once('knjl326fQuery.inc');

class knjl326fController extends Controller {
    var $ModelClassName = "knjl326fModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl326f":
                    $sessionInstance->knjl326fModel();
                    $this->callView("knjl326fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl326fCtl = new knjl326fController;
//var_dump($_REQUEST);
?>
