<?php

require_once('for_php7.php');

require_once('knjd260Model.inc');
require_once('knjd260Query.inc');

class knjd260Controller extends Controller {
    var $ModelClassName = "knjd260Model";
    var $ProgramID      = "KNJD260";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd260":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd260Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd260Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd260Ctl = new knjd260Controller;
var_dump($_REQUEST);
?>
