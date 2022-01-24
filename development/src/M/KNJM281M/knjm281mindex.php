<?php

require_once('for_php7.php');

require_once('knjm281mModel.inc');
require_once('knjm281mQuery.inc');

class knjm281mController extends Controller {
    var $ModelClassName = "knjm281mModel";
    var $ProgramID      = "KNJM281M";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm281mForm1");
                    }
                    break 2;
                case "":
                case "sel":
                    $this->callView("knjm281mForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm281mCtl = new knjm281mController;
//var_dump($_REQUEST);
?>
