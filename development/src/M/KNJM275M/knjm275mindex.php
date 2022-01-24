<?php

require_once('for_php7.php');

require_once('knjm275mModel.inc');
require_once('knjm275mQuery.inc');

class knjm275mController extends Controller {
    var $ModelClassName = "knjm275mModel";
    var $ProgramID      = "KNJM275M";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm275mForm1");
                    }
                    break 2;
                case "":
                case "sel":
                    $this->callView("knjm275mForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm275mCtl = new knjm275mController;
//var_dump($_REQUEST);
?>
