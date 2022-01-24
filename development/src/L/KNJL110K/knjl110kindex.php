<?php

require_once('for_php7.php');

require_once('knjl110kModel.inc');
require_once('knjl110kQuery.inc');

class knjl110kController extends Controller {
    var $ModelClassName = "knjl110kModel";
    var $ProgramID      = "KNJL110K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl110kForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl110kForm1");
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
$knjl110kCtl = new knjl110kController;
?>
