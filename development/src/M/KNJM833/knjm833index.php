<?php

require_once('for_php7.php');

require_once('knjm833Model.inc');
require_once('knjm833Query.inc');

class knjm833Controller extends Controller {
    var $ModelClassName = "knjm833Model";
    var $ProgramID      = "KNJM833";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm833":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm833Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm833Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm833Ctl = new knjm833Controller;
//var_dump($_REQUEST);
?>

