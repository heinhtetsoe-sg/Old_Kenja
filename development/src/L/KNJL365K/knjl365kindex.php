<?php
require_once('knjl365kModel.inc');
require_once('knjl365kQuery.inc');

class knjl365kController extends Controller {
    var $ModelClassName = "knjl365kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl365k":
                    $sessionInstance->knjl365kModel();
                    $this->callView("knjl365kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl365kCtl = new knjl365kController;
var_dump($_REQUEST);
?>
