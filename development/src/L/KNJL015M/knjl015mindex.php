<?php

require_once('for_php7.php');

require_once('knjl015mModel.inc');
require_once('knjl015mQuery.inc');

class knjl015mController extends Controller {
    var $ModelClassName = "knjl015mModel";
    var $ProgramID      = "KNJL015M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl015m":
                    $sessionInstance->knjl015mModel();
                    $this->callView("knjl015mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl015mForm1");
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
$knjl015mCtl = new knjl015mController;
?>
