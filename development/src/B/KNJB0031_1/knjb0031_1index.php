<?php

require_once('for_php7.php');

require_once('knjb0031_1Model.inc');
require_once('knjb0031_1Query.inc');

class knjb0031_1Controller extends Controller {
    var $ModelClassName = "knjb0031_1Model";
    var $ProgramID      = "KNJB0031";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                    $this->callView("knjb0031_1Form1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "csv": // csv
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjb0031_1Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }

}
$knjb0031_1Ctl = new knjb0031_1Controller;
//var_dump($_REQUEST);
?>
