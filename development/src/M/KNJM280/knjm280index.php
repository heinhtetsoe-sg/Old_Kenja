<?php

require_once('for_php7.php');

require_once('knjm280Model.inc');
require_once('knjm280Query.inc');

class knjm280Controller extends Controller {
    var $ModelClassName = "knjm280Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjm280Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm280Form1");
                    exit;
                case "dsub":
                    $this->callView("knjm280Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm280Ctl = new knjm280Controller;
//var_dump($_REQUEST);
?>
