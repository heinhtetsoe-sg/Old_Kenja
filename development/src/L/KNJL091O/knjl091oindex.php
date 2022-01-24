<?php

require_once('for_php7.php');

require_once('knjl091oModel.inc');
require_once('knjl091oQuery.inc');

class knjl091oController extends Controller {
    var $ModelClassName = "knjl091oModel";
    var $ProgramID      = "KNJL091O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl091oForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl091oForm1");
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
$knjl091oCtl = new knjl091oController;
?>
