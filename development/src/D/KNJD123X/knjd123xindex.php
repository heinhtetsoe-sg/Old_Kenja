<?php

require_once('for_php7.php');

require_once('knjd123xModel.inc');
require_once('knjd123xQuery.inc');

class knjd123xController extends Controller {
    var $ModelClassName = "knjd123xModel";
    var $ProgramID      = "KNJD123X";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd123xForm1");
                    }
                    break 2;
                case "":
                case "knjd123x":
                    $sessionInstance->knjd123xModel();
                    $this->callView("knjd123xForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd123xCtl = new knjd123xController;
//var_dump($_REQUEST);
?>
