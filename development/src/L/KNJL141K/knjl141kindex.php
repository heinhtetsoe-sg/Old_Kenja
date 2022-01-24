<?php

require_once('for_php7.php');

require_once('knjl141kModel.inc');
require_once('knjl141kQuery.inc');

class knjl141kController extends Controller {
    var $ModelClassName = "knjl141kModel";
    var $ProgramID      = "KNJL141K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl141kForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl141kForm1");
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
$knjl141kCtl = new knjl141kController;
?>
