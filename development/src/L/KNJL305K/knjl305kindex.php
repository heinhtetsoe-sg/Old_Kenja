<?php
require_once('knjl305kModel.inc');
require_once('knjl305kQuery.inc');

class knjl305kController extends Controller {
    var $ModelClassName = "knjl305kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305k":
                    $sessionInstance->knjl305kModel();
                    $this->callView("knjl305kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl305kCtl = new knjl305kController;
var_dump($_REQUEST);
?>
