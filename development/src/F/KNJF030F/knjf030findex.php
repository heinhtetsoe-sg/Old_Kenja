<?php

require_once('for_php7.php');

require_once('knjf030fModel.inc');
require_once('knjf030fQuery.inc');

class knjf030fController extends Controller {
    var $ModelClassName = "knjf030fModel";
    var $ProgramID        = "KNJF030F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf030f":                             //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //クラス変更時のSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf030fModel();       //コントロールマスタの呼び出し
                    $this->callView("knjf030fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf030fCtl = new knjf030fController;
var_dump($_REQUEST);
?>
