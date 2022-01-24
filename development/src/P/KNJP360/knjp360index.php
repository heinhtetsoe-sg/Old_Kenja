<?php

require_once('for_php7.php');

require_once('knjp360Model.inc');
require_once('knjp360Query.inc');

class knjp360Controller extends Controller {
    var $ModelClassName = "knjp360Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //NO002
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp360Form1");
                    }
                    break 2;
                case "":
                case "knjp360":
                    $sessionInstance->knjp360Model();
                    $this->callView("knjp360Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp360Ctl = new knjp360Controller;
//var_dump($_REQUEST);
?>
