<?php

require_once('for_php7.php');

require_once('knjl570hModel.inc');
require_once('knjl570hQuery.inc');

class knjl570hController extends Controller {
    var $ModelClassName = "knjl570hModel";
    var $ProgramID      = "KNJL570H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl570hForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl570hForm1");
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
$knjl570hCtl = new knjl570hController;
?>
