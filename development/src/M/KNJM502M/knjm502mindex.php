<?php

require_once('for_php7.php');

require_once('knjm502mModel.inc');
require_once('knjm502mQuery.inc');

class knjm502mController extends Controller {
    var $ModelClassName = "knjm502mModel";
    var $ProgramID      = "KNJM502M";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm502mForm1");
                    }
                    break 2;
                case "":
                case "sel":
                    $this->callView("knjm502mForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm502mCtl = new knjm502mController;
//var_dump($_REQUEST);
?>
