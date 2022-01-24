<?php

require_once('for_php7.php');

require_once('knjl344cModel.inc');
require_once('knjl344cQuery.inc');

class knjl344cController extends Controller {
    var $ModelClassName = "knjl344cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl344c":
                    $sessionInstance->knjl344cModel();
                    $this->callView("knjl344cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl344cCtl = new knjl344cController;
//var_dump($_REQUEST);
?>
