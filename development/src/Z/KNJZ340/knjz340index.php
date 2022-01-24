<?php

require_once('for_php7.php');

require_once('knjz340Model.inc');
require_once('knjz340Query.inc');

class knjz340Controller extends Controller {
    var $ModelClassName = "knjz340Model";
    var $ProgramID      = "KNJZ340";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz340":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjz340Model();		//コントロールマスタの呼び出し
                    $this->callView("knjz340Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjz340Ctl = new knjz340Controller;
var_dump($_REQUEST);
?>
