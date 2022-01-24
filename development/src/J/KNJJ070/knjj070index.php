<?php

require_once('for_php7.php');

require_once('knjj070Model.inc');
require_once('knjj070Query.inc');

class knjj070Controller extends Controller {
    var $ModelClassName = "knjj070Model";
    var $ProgramID      = "KNJJ070";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj070":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjj070Model();		//コントロールマスタの呼び出し
                    $this->callView("knjj070Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjj070Ctl = new knjj070Controller;
var_dump($_REQUEST);
?>
