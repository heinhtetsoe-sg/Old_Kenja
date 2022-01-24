<?php

require_once('for_php7.php');

require_once('knjl309eModel.inc');
require_once('knjl309eQuery.inc');

class knjl309eController extends Controller {
    var $ModelClassName = "knjl309eModel";
    var $ProgramID      = "KNJL309E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl309e":
                    $this->callView("knjl309eForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl309eForm1");
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
$knjl309eCtl = new knjl309eController;
?>
