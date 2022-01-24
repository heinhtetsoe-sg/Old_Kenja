<?php

require_once('for_php7.php');

require_once('knjl256cModel.inc');
require_once('knjl256cQuery.inc');

class knjl256cController extends Controller {
    var $ModelClassName = "knjl256cModel";
    var $ProgramID      = "KNJL256C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl256c":
                    $sessionInstance->knjl256cModel();
                    $this->callView("knjl256cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl256cForm1");
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
$knjl256cCtl = new knjl256cController;
//var_dump($_REQUEST);
?>
