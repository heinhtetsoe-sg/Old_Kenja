<?php

require_once('for_php7.php');

require_once('knjl348kModel.inc');
require_once('knjl348kQuery.inc');

class knjl348kController extends Controller {
    var $ModelClassName = "knjl348kModel";
    var $ProgramID      = "KNJL348K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl348kForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl348kForm1");
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
$knjl348kCtl = new knjl348kController;
?>
