<?php

require_once('for_php7.php');

require_once('knjl327fModel.inc');
require_once('knjl327fQuery.inc');

class knjl327fController extends Controller {
    var $ModelClassName = "knjl327fModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327f":
                    $sessionInstance->knjl327fModel();
                    $this->callView("knjl327fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl327fCtl = new knjl327fController;
//var_dump($_REQUEST);
?>
