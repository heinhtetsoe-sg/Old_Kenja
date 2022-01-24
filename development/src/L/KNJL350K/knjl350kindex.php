<?php
require_once('knjl350kModel.inc');
require_once('knjl350kQuery.inc');

class knjl350kController extends Controller {
    var $ModelClassName = "knjl350kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl350k":
                    $sessionInstance->knjl350kModel();
                    $this->callView("knjl350kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl350kCtl = new knjl350kController;
var_dump($_REQUEST);
?>
