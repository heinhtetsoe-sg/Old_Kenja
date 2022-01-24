<?php

require_once('for_php7.php');

require_once('knjl300mModel.inc');
require_once('knjl300mQuery.inc');

class knjl300mController extends Controller {
    var $ModelClassName = "knjl300mModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300m":
                    $this->callView("knjl300mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl300mCtl = new knjl300mController;
//var_dump($_REQUEST);
?>
