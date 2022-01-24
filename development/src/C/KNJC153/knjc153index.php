<?php

require_once('for_php7.php');

require_once('knjc153Model.inc');
require_once('knjc153Query.inc');

class knjc153Controller extends Controller {
    var $ModelClassName = "knjc153Model";
    var $ProgramID      = "KNJC153";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc153":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc153Model();        //コントロールマスタの呼び出し
                    $this->callView("knjc153Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc153Ctl = new knjc153Controller;
//var_dump($_REQUEST);
?>
