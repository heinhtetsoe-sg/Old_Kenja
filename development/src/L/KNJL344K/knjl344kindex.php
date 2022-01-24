<?php

require_once('for_php7.php');

require_once('knjl344kModel.inc');
require_once('knjl344kQuery.inc');

class knjl344kController extends Controller {
    var $ModelClassName = "knjl344kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl344k":
                    $sessionInstance->knjl344kModel();
                    $this->callView("knjl344kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl344kCtl = new knjl344kController;
var_dump($_REQUEST);
?>
