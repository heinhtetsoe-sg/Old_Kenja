<?php

require_once('for_php7.php');

require_once('knjj140Model.inc');
require_once('knjj140Query.inc');

class knjj140Controller extends Controller {
    var $ModelClassName = "knjj140Model";
    var $ProgramID      = "KNJJ140";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj140":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjj140Model();        //コントロールマスタの呼び出し
                    $this->callView("knjj140Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjj140Ctl = new knjj140Controller;
//var_dump($_REQUEST);
?>
