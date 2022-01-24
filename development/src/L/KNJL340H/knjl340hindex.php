<?php

require_once('for_php7.php');

require_once('knjl340hModel.inc');
require_once('knjl340hQuery.inc');

class knjl340hController extends Controller {
    var $ModelClassName = "knjl340hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl340h":
                    $sessionInstance->knjl340hModel();
                    $this->callView("knjl340hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl340hCtl = new knjl340hController;
var_dump($_REQUEST);
?>
