<?php

require_once('for_php7.php');

require_once('knjm290Model.inc');
require_once('knjm290Query.inc');

class knjm290Controller extends Controller {
    var $ModelClassName = "knjm290Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "nenhenkou":
                    $sessionInstance->knjm290Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm290Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm290Ctl = new knjm290Controller;
//var_dump($_REQUEST);
?>
