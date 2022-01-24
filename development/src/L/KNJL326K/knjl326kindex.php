<?php

require_once('for_php7.php');

require_once('knjl326kModel.inc');
require_once('knjl326kQuery.inc');

class knjl326kController extends Controller {
    var $ModelClassName = "knjl326kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl326k":
                    $sessionInstance->knjl326kModel();
                    $this->callView("knjl326kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl326kCtl = new knjl326kController;
var_dump($_REQUEST);
?>
