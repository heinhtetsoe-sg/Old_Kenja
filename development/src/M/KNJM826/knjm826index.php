<?php

require_once('for_php7.php');

require_once('knjm826Model.inc');
require_once('knjm826Query.inc');

class knjm826Controller extends Controller {
    var $ModelClassName = "knjm826Model";
    var $ProgramID      = "KNJM826";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm826":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm826Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm826Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm826Ctl = new knjm826Controller;
//var_dump($_REQUEST);
?>
