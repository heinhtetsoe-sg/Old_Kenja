<?php
require_once('knjl300kModel.inc');
require_once('knjl300kQuery.inc');

class knjl300kController extends Controller {
    var $ModelClassName = "knjl300kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300k":
                    $sessionInstance->knjl300kModel();
                    $this->callView("knjl300kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl300kCtl = new knjl300kController;
var_dump($_REQUEST);
?>
