<?php

require_once('for_php7.php');

require_once('knjl358cModel.inc');
require_once('knjl358cQuery.inc');

class knjl358cController extends Controller {
    var $ModelClassName = "knjl358cModel";
    var $ProgramID      = "KNJL358C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl358c":
                    $sessionInstance->knjl358cModel();
                    $this->callView("knjl358cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl358cForm1");
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
$knjl358cCtl = new knjl358cController;
//var_dump($_REQUEST);
?>
