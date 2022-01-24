<?php

require_once('for_php7.php');

require_once('knjl303hModel.inc');
require_once('knjl303hQuery.inc');

class knjl303hController extends Controller {
    var $ModelClassName = "knjl303hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl303h":
                    $sessionInstance->knjl303hModel();
                    $this->callView("knjl303hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl303hCtl = new knjl303hController;
var_dump($_REQUEST);
?>
