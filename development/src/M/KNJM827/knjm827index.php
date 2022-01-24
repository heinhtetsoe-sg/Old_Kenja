<?php

require_once('for_php7.php');

require_once('knjm827Model.inc');
require_once('knjm827Query.inc');

class knjm827Controller extends Controller {
    var $ModelClassName = "knjm827Model";
    var $ProgramID      = "KNJM827";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm827":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm827Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm827Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm827Ctl = new knjm827Controller;
//var_dump($_REQUEST);
?>

