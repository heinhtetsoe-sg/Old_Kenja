<?php

require_once('for_php7.php');

require_once('knjl140tModel.inc');
require_once('knjl140tQuery.inc');

class knjl140tController extends Controller {
    var $ModelClassName = "knjl140tModel";
    var $ProgramID      = "KNJL140T";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl140tForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl140tForm1");
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
$knjl140tCtl = new knjl140tController;
?>
