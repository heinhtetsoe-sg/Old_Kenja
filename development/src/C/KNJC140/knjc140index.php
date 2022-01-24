<?php

require_once('for_php7.php');

require_once('knjc140Model.inc');
require_once('knjc140Query.inc');

class knjc140Controller extends Controller {
    var $ModelClassName = "knjc140Model";
    var $ProgramID      = "KNJC140";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc140":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc140Model();        //コントロールマスタの呼び出し
                    $this->callView("knjc140Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc140Ctl = new knjc140Controller;
var_dump($_REQUEST);
?>
