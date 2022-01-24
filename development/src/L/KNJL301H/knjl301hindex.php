<?php

require_once('for_php7.php');

require_once('knjl301hModel.inc');
require_once('knjl301hQuery.inc');

class knjl301hController extends Controller {
    var $ModelClassName = "knjl301hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301h":
                    $sessionInstance->knjl301hModel();
                    $this->callView("knjl301hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl301hCtl = new knjl301hController;
var_dump($_REQUEST);
?>
