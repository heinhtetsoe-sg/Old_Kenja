<?php

require_once('for_php7.php');

require_once('knjm560Model.inc');
require_once('knjm560Query.inc');

class knjm560Controller extends Controller {
    var $ModelClassName = "knjm560Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjm560Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm560Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm560Ctl = new knjm560Controller;
var_dump($_REQUEST);
?>
