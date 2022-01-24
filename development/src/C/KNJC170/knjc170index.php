<?php

require_once('for_php7.php');

require_once('knjc170Model.inc');
require_once('knjc170Query.inc');

class knjc170Controller extends Controller {
    var $ModelClassName = "knjc170Model";
    var $ProgramID      = "KNJC170";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc170":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjc170Model();		//コントロールマスタの呼び出し
                    $this->callView("knjc170Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjc170Ctl = new knjc170Controller;
var_dump($_REQUEST);
?>
