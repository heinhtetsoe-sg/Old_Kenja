<?php

require_once('for_php7.php');

require_once('knjl140sModel.inc');
require_once('knjl140sQuery.inc');

class knjl140sController extends Controller {
    var $ModelClassName = "knjl140sModel";
    var $ProgramID      = "KNJL140S";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl140sForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl140sForm1");
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
$knjl140sCtl = new knjl140sController;
?>
