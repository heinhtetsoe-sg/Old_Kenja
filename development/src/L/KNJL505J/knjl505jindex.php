<?php

require_once('for_php7.php');

require_once('knjl505jModel.inc');
require_once('knjl505jQuery.inc');

class knjl505jController extends Controller {
    var $ModelClassName = "knjl505jModel";
    var $ProgramID      = "KNJL505J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl505jForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl505jForm1");
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
$knjl505jCtl = new knjl505jController;
?>
