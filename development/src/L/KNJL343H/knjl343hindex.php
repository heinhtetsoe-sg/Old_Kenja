<?php

require_once('for_php7.php');

require_once('knjl343hModel.inc');
require_once('knjl343hQuery.inc');

class knjl343hController extends Controller {
    var $ModelClassName = "knjl343hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343h":
                    $sessionInstance->knjl343hModel();
                    $this->callView("knjl343hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl343hCtl = new knjl343hController;
var_dump($_REQUEST);
?>
