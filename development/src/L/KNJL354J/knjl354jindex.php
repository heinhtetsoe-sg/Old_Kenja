<?php
require_once('knjl354jModel.inc');
require_once('knjl354jQuery.inc');

class knjl354jController extends Controller {
    var $ModelClassName = "knjl354jModel";
    var $ProgramID      = "KNJL354J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl354j":
                    $sessionInstance->knjl354jModel();
                    $this->callView("knjl354jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl354jCtl = new knjl354jController;
var_dump($_REQUEST);
?>
