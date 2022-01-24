<?php

require_once('for_php7.php');

require_once('knjl310hModel.inc');
require_once('knjl310hQuery.inc');

class knjl310hController extends Controller {
    var $ModelClassName = "knjl310hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl310h":
                    $sessionInstance->knjl310hModel();
                    $this->callView("knjl310hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl310hCtl = new knjl310hController;
var_dump($_REQUEST);
?>
