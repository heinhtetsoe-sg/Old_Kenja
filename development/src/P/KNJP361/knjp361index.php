<?php

require_once('for_php7.php');

require_once('knjp361Model.inc');
require_once('knjp361Query.inc');

class knjp361Controller extends Controller {
    var $ModelClassName = "knjp361Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //NO002
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp361Form1");
                    }
                    break 2;
                case "":
                case "knjp361":
                    $sessionInstance->knjp361Model();
                    $this->callView("knjp361Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp361Ctl = new knjp361Controller;
//var_dump($_REQUEST);
?>
