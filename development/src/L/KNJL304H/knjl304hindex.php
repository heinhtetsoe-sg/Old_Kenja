<?php
require_once('knjl304hModel.inc');
require_once('knjl304hQuery.inc');

class knjl304hController extends Controller {
    var $ModelClassName = "knjl304hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl304h":
                    $sessionInstance->knjl304hModel();
                    $this->callView("knjl304hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl304hCtl = new knjl304hController;
var_dump($_REQUEST);
?>
