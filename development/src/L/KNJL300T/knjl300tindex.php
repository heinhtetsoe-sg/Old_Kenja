<?php

require_once('for_php7.php');

require_once('knjl300tModel.inc');
require_once('knjl300tQuery.inc');

class knjl300tController extends Controller {
    var $ModelClassName = "knjl300tModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300t":
                    $this->callView("knjl300tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl300tCtl = new knjl300tController;
//var_dump($_REQUEST);
?>
