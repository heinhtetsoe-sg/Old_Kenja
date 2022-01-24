<?php

require_once('for_php7.php');

require_once('knjm360Model.inc');
require_once('knjm360Query.inc');

class knjm360Controller extends Controller {
    var $ModelClassName = "knjm360Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjm360Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm360Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm360Ctl = new knjm360Controller;
//var_dump($_REQUEST);
?>
