<?php

require_once('for_php7.php');

require_once('knjl315hModel.inc');
require_once('knjl315hQuery.inc');

class knjl315hController extends Controller {
    var $ModelClassName = "knjl315hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl315h":
                    $sessionInstance->knjl315hModel();
                    $this->callView("knjl315hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl315hCtl = new knjl315hController;
var_dump($_REQUEST);
?>
