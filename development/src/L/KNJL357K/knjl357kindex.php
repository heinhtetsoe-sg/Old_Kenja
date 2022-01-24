<?php
require_once('knjl357kModel.inc');
require_once('knjl357kQuery.inc');

class knjl357kController extends Controller {
    var $ModelClassName = "knjl357kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl357k":
                    $sessionInstance->knjl357kModel();
                    $this->callView("knjl357kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl357kCtl = new knjl357kController;
var_dump($_REQUEST);
?>
