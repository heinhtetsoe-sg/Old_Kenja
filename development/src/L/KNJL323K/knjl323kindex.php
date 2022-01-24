<?php

require_once('for_php7.php');

require_once('knjl323kModel.inc');
require_once('knjl323kQuery.inc');

class knjl323kController extends Controller {
    var $ModelClassName = "knjl323kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl323k":
                    $sessionInstance->knjl323kModel();
                    $this->callView("knjl323kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl323kCtl = new knjl323kController;
var_dump($_REQUEST);
?>
