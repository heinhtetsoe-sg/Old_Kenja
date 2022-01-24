<?php

require_once('for_php7.php');

require_once('knjl324cModel.inc');
require_once('knjl324cQuery.inc');

class knjl324cController extends Controller {
    var $ModelClassName = "knjl324cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324c":
                    $sessionInstance->knjl324cModel();
                    $this->callView("knjl324cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl324cCtl = new knjl324cController;
//var_dump($_REQUEST);
?>
