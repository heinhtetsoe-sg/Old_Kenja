<?php

require_once('for_php7.php');

require_once('knjl410yModel.inc');
require_once('knjl410yQuery.inc');

class knjl410yController extends Controller {
    var $ModelClassName = "knjl410yModel";
    var $ProgramID      = "KNJL410Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl410yForm1");
                    }
                    break 2;
                case "":
                case "knjl410y":
                case "change":
                    $sessionInstance->knjl410yModel();
                    $this->callView("knjl410yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl410yCtl = new knjl410yController;
?>
