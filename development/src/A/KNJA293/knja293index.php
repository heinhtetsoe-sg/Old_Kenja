<?php

require_once('for_php7.php');

require_once('knja293Model.inc');
require_once('knja293Query.inc');

class knja293Controller extends Controller {
    var $ModelClassName = "knja293Model";
    var $ProgramID      = "KNJA293";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja293":
                    $sessionInstance->knja293Model();
                    $this->callView("knja293Form1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja293Form1");
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
$knja293Ctl = new knja293Controller;
//var_dump($_REQUEST);
?>
