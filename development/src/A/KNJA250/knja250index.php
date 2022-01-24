<?php

require_once('for_php7.php');

require_once('knja250Model.inc');
require_once('knja250Query.inc');

class knja250Controller extends Controller {
    var $ModelClassName = "knja250Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knja250Model();       //コントロールマスタの呼び出し
                    $this->callView("knja250Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja250Ctl = new knja250Controller;
var_dump($_REQUEST);
?>
