<?php

require_once('for_php7.php');

require_once('knjh302Model.inc');
require_once('knjh302Query.inc');

class knjh302Controller extends Controller {
    var $ModelClassName = "knjh302Model";
    var $ProgramID      = "KNJH302";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh302":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjh302Model();		//コントロールマスタの呼び出し
                    $this->callView("knjh302Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh302Ctl = new knjh302Controller;
?>
