<?php

require_once('for_php7.php');

require_once('knjl374jModel.inc');
require_once('knjl374jQuery.inc');

class knjl374jController extends Controller {
    var $ModelClassName = "knjl374jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl374j":
                    $sessionInstance->knjl374jModel();
                    $this->callView("knjl374jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl374jCtl = new knjl374jController;
var_dump($_REQUEST);
?>
