<?php
require_once('knjm438wModel.inc');
require_once('knjm438wQuery.inc');

class knjm438wController extends Controller {
    var $ModelClassName = "knjm438wModel";
    var $ProgramID      = "KNJM438W";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm438wForm1");
                    }
                    break 2;
                case "":
                case "sel":
                    $this->callView("knjm438wForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm438wCtl = new knjm438wController;
//var_dump($_REQUEST);
?>
