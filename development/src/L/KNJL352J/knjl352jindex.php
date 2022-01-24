<?php

require_once('for_php7.php');

require_once('knjl352jModel.inc');
require_once('knjl352jQuery.inc');

class knjl352jController extends Controller {
    var $ModelClassName = "knjl352jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl352j":
                    $sessionInstance->knjl352jModel();
                    $this->callView("knjl352jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl352jCtl = new knjl352jController;
var_dump($_REQUEST);
?>
