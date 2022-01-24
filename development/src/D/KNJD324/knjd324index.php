<?php

require_once('for_php7.php');

require_once('knjd324Model.inc');
require_once('knjd324Query.inc');

class knjd324Controller extends Controller {
    var $ModelClassName = "knjd324Model";
    var $ProgramID      = "KNJD324";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd324":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd324Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd324Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd324Ctl = new knjd324Controller;
var_dump($_REQUEST);
?>
