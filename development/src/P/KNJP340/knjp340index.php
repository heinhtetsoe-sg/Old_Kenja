<?php

require_once('for_php7.php');

require_once('knjp340Model.inc');
require_once('knjp340Query.inc');

class knjp340Controller extends Controller {
    var $ModelClassName = "knjp340Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp340Form1");
                    }
                    break 2;
                case "":
                case "knjp340":
                    $sessionInstance->knjp340Model();
                    $this->callView("knjp340Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp340Ctl = new knjp340Controller;
//var_dump($_REQUEST);
?>
