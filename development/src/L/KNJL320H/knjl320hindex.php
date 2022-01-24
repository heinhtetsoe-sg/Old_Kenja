<?php

require_once('for_php7.php');

require_once('knjl320hModel.inc');
require_once('knjl320hQuery.inc');

class knjl320hController extends Controller {
    var $ModelClassName = "knjl320hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl320h":
                    $sessionInstance->knjl320hModel();
                    $this->callView("knjl320hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl320hCtl = new knjl320hController;
var_dump($_REQUEST);
?>
