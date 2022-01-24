<?php

require_once('for_php7.php');

require_once('knjl352Model.inc');
require_once('knjl352Query.inc');

class knjl352Controller extends Controller {
    var $ModelClassName = "knjl352Model";
    var $ProgramID      = "KNJL352";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl352":
                case "read":
                    $sessionInstance->knjl352Model();
                    $this->callView("knjl352Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl352Form1");
                    }
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl352Ctl = new knjl352Controller;
//var_dump($_REQUEST);
?>
