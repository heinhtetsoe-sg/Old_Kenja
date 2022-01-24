<?php

require_once('for_php7.php');

require_once('knjc166cModel.inc');
require_once('knjc166cQuery.inc');

class knjc166cController extends Controller {
    var $ModelClassName = "knjc166cModel";
    var $ProgramID      = "KNJC166C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc166c":    //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc166cModel();		//コントロールマスタの呼び出し
                    $this->callView("knjc166cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc166cCtl = new knjc166cController;
var_dump($_REQUEST);
?>
