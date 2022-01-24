<?php

require_once('for_php7.php');

require_once('knjz155tModel.inc');
require_once('knjz155tQuery.inc');

class knjz155tController extends Controller {
    var $ModelClassName = "knjz155tModel";
    var $ProgramID      = "KNJZ155T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz155t":					        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
					$sessionInstance->knjz155tModel();		//コントロールマスタの呼び出し
                    $this->callView("knjz155tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjz155tCtl = new knjz155tController;
var_dump($_REQUEST);
?>
