<?php

require_once('for_php7.php');

require_once('knjl141tModel.inc');
require_once('knjl141tQuery.inc');

class knjl141tController extends Controller {
    var $ModelClassName = "knjl141tModel";
    var $ProgramID      = "KNJL141T";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl141tForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl141tForm1");
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
$knjl141tCtl = new knjl141tController;
?>
