<?php

require_once('for_php7.php');

require_once('knjm831Model.inc');
require_once('knjm831Query.inc');

class knjm831Controller extends Controller {
    var $ModelClassName = "knjm831Model";
    var $ProgramID      = "KNJM831";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm831":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm831Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm831Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm831Ctl = new knjm831Controller;
//var_dump($_REQUEST);
?>

