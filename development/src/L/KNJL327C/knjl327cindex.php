<?php

require_once('for_php7.php');

require_once('knjl327cModel.inc');
require_once('knjl327cQuery.inc');

class knjl327cController extends Controller {
    var $ModelClassName = "knjl327cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327c":
                    $sessionInstance->knjl327cModel();
                    $this->callView("knjl327cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl327cCtl = new knjl327cController;
//var_dump($_REQUEST);
?>
