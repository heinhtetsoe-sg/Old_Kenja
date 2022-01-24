<?php

require_once('for_php7.php');

require_once('knjl352kModel.inc');
require_once('knjl352kQuery.inc');

class knjl352kController extends Controller {
    var $ModelClassName = "knjl352kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl352k":
                    $sessionInstance->knjl352kModel();
                    $this->callView("knjl352kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl352kCtl = new knjl352kController;
var_dump($_REQUEST);
?>
