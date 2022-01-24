<?php

require_once('for_php7.php');

require_once('knjl302eModel.inc');
require_once('knjl302eQuery.inc');

class knjl302eController extends Controller {
    var $ModelClassName = "knjl302eModel";
    var $ProgramID      = "KNJL302E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl302e":
                    $this->callView("knjl302eForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl302eForm1");
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
$knjl302eCtl = new knjl302eController;
?>
