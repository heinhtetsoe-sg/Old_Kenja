<?php

require_once('for_php7.php');

require_once('knja321Model.inc');
require_once('knja321Query.inc');

class knja321Controller extends Controller {
    var $ModelClassName = "knja321Model";
    var $ProgramID      = "KNJA321";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja321":
                    $sessionInstance->knja321Model();
                    $this->callView("knja321Form1");
                    exit;
                case "template_dl":
                    $sessionInstance->gettemplateDownloadModel();
                    $this->callView("knja321Form1");
                    break 2;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja321Form1");
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
$knja321Ctl = new knja321Controller;
//var_dump($_REQUEST);
?>
