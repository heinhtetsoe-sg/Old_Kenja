<?php

require_once('for_php7.php');

require_once('knjm438mModel.inc');
require_once('knjm438mQuery.inc');

class knjm438mController extends Controller {
    var $ModelClassName = "knjm438mModel";
    var $ProgramID      = "KNJM438M";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm438mForm1");
                    }
                    break 2;
                case "":
                case "sel":
                    $this->callView("knjm438mForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm438mCtl = new knjm438mController;
//var_dump($_REQUEST);
?>
