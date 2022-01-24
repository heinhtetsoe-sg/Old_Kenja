<?php

require_once('for_php7.php');

require_once('knjl370mModel.inc');
require_once('knjl370mQuery.inc');

class knjl370mController extends Controller {
    var $ModelClassName = "knjl370mModel";
    var $ProgramID      = "KNJL370M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl370m":
                    $sessionInstance->knjl370mModel();
                    $this->callView("knjl370mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl370mForm1");
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
$knjl370mCtl = new knjl370mController;
?>
