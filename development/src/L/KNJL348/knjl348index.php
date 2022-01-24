<?php

require_once('for_php7.php');

require_once('knjl348Model.inc');
require_once('knjl348Query.inc');

class knjl348Controller extends Controller {
    var $ModelClassName = "knjl348Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl348":
                    $sessionInstance->knjl348Model();
                    $this->callView("knjl348Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl348Form1");
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
$knjl348Ctl = new knjl348Controller;
//var_dump($_REQUEST);
?>
