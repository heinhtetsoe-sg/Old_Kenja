<?php

require_once('for_php7.php');

require_once('knjf030dModel.inc');
require_once('knjf030dQuery.inc');

class knjf030dController extends Controller {
    var $ModelClassName = "knjf030dModel";
    var $ProgramID        = "KNJF030D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf030d":                             //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //クラス変更時のSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf030dModel();       //コントロールマスタの呼び出し
                    $this->callView("knjf030dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf030dCtl = new knjf030dController;
var_dump($_REQUEST);
?>
