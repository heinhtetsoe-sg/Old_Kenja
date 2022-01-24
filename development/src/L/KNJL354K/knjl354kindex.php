<?php
require_once('knjl354kModel.inc');
require_once('knjl354kQuery.inc');

class knjl354kController extends Controller {
    var $ModelClassName = "knjl354kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl354k":
                    $sessionInstance->knjl354kModel();
                    $this->callView("knjl354kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl354kCtl = new knjl354kController;
var_dump($_REQUEST);
?>
