<?php

require_once('for_php7.php');

require_once('knjl343cModel.inc');
require_once('knjl343cQuery.inc');

class knjl343cController extends Controller {
    var $ModelClassName = "knjl343cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343c":
                    $sessionInstance->knjl343cModel();
                    $this->callView("knjl343cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl343cCtl = new knjl343cController;
//var_dump($_REQUEST);
?>
