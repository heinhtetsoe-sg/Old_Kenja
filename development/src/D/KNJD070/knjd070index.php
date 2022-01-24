<?php

require_once('for_php7.php');

require_once('knjd070Model.inc');
require_once('knjd070Query.inc');

class knjd070Controller extends Controller {
    var $ModelClassName = "knjd070Model";
    var $ProgramID      = "KNJD070";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd070":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd070Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd070Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd070Ctl = new knjd070Controller;
var_dump($_REQUEST);
?>
