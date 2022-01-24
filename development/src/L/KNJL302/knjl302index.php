<?php

require_once('for_php7.php');

require_once('knjl302Model.inc');
require_once('knjl302Query.inc');

class knjl302Controller extends Controller {
    var $ModelClassName = "knjl302Model";
    var $ProgramID      = "KNJL302";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl302":
                    $sessionInstance->knjl302Model();
                    $this->callView("knjl302Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl302Form1");
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
$knjl302Ctl = new knjl302Controller;
//var_dump($_REQUEST);
?>
