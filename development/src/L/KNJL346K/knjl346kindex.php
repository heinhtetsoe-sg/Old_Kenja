<?php
require_once('knjl346kModel.inc');
require_once('knjl346kQuery.inc');

class knjl346kController extends Controller {
    var $ModelClassName = "knjl346kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl346k":
                    $sessionInstance->knjl346kModel();
                    $this->callView("knjl346kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl346kCtl = new knjl346kController;
var_dump($_REQUEST);
?>
