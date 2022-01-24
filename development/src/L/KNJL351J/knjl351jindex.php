<?php

require_once('for_php7.php');

require_once('knjl351jModel.inc');
require_once('knjl351jQuery.inc');

class knjl351jController extends Controller {
    var $ModelClassName = "knjl351jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl351j":
                    $sessionInstance->knjl351jModel();
                    $this->callView("knjl351jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl351jCtl = new knjl351jController;
var_dump($_REQUEST);
?>
