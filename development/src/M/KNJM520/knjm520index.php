<?php

require_once('for_php7.php');

require_once('knjm520Model.inc');
require_once('knjm520Query.inc');

class knjm520Controller extends Controller {
    var $ModelClassName = "knjm520Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm520":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm520Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm520Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm520Ctl = new knjm520Controller;
var_dump($_REQUEST);
?>
