<?php
require_once('knjd126sModel.inc');
require_once('knjd126sQuery.inc');

class knjd126sController extends Controller {
    var $ModelClassName = "knjd126sModel";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjd126sForm1");
                   break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd126sForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd126sCtl = new knjd126sController;
//var_dump($_REQUEST);
?>
