<?php

require_once('for_php7.php');

require_once('knja320aModel.inc');
require_once('knja320aQuery.inc');

class knja320aController extends Controller {
    var $ModelClassName = "knja320aModel";
    var $ProgramID      = "KNJA320A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja320a":
                    $sessionInstance->knja320aModel();
                    $this->callView("knja320aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja320aForm1");
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
$knja320aCtl = new knja320aController;
//var_dump($_REQUEST);
?>
