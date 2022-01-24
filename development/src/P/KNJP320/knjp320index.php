<?php

require_once('for_php7.php');

require_once('knjp320Model.inc');
require_once('knjp320Query.inc');

class knjp320Controller extends Controller {
    var $ModelClassName = "knjp320Model";
    var $ProgramID      = "KNJp320";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp320":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjp320Model();		//コントロールマスタの呼び出し
                    $this->callView("knjp320Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjp320Ctl = new knjp320Controller;
var_dump($_REQUEST);
?>
