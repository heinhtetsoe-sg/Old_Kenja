<?php

require_once('for_php7.php');

require_once('knjl076pModel.inc');
require_once('knjl076pQuery.inc');

class knjl076pController extends Controller {
    var $ModelClassName = "knjl076pModel";
    var $ProgramID      = "KNJL076P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl076pForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl076pForm1");
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
$knjl076pCtl = new knjl076pController;
?>
