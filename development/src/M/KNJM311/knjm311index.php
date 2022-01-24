<?php

require_once('for_php7.php');

require_once('knjm311Model.inc');
require_once('knjm311Query.inc');

class knjm311Controller extends Controller {
    var $ModelClassName = "knjm311Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "nenhenkou":
                    $sessionInstance->knjm311Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm311Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm311Ctl = new knjm311Controller;
//var_dump($_REQUEST);
?>
