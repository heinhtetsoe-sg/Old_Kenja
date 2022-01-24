<?php

require_once('for_php7.php');

require_once('knjl141mModel.inc');
require_once('knjl141mQuery.inc');

class knjl141mController extends Controller {
    var $ModelClassName = "knjl141mModel";
    var $ProgramID      = "KNJL141M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl141mForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl141mForm1");
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
$knjl141mCtl = new knjl141mController;
?>
