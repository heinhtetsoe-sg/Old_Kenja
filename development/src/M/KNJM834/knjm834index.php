<?php

require_once('for_php7.php');

require_once('knjm834Model.inc');
require_once('knjm834Query.inc');

class knjm834Controller extends Controller {
    var $ModelClassName = "knjm834Model";
    var $ProgramID      = "KNJM834";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm834":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm834Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm834Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm834Ctl = new knjm834Controller;
//var_dump($_REQUEST);
?>

