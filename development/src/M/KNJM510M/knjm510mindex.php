<?php

require_once('for_php7.php');

require_once('knjm510mModel.inc');
require_once('knjm510mQuery.inc');

class knjm510mController extends Controller {
    var $ModelClassName = "knjm510mModel";
    var $ProgramID      = "KNJM510M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm510m":
                    $sessionInstance->knjm510mModel();
                    $this->callView("knjm510mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm510mCtl = new knjm510mController;
//var_dump($_REQUEST);
?>
