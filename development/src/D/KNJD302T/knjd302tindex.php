<?php

require_once('for_php7.php');

require_once('knjd302tModel.inc');
require_once('knjd302tQuery.inc');

class knjd302tController extends Controller {
    var $ModelClassName = "knjd302tModel";
    var $ProgramID      = "KNJD302T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd302t":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd302tModel();		//コントロールマスタの呼び出し
                    $this->callView("knjd302tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd302tCtl = new knjd302tController;
var_dump($_REQUEST);
?>
