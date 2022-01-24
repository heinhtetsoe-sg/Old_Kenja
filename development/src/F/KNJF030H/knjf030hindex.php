<?php

require_once('for_php7.php');

require_once('knjf030hModel.inc');
require_once('knjf030hQuery.inc');

class knjf030hController extends Controller {
    var $ModelClassName = "knjf030hModel";
    var $ProgramID        = "KNJF030H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf030h":                             //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //クラス変更時のSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf030hModel();       //コントロールマスタの呼び出し
                    $this->callView("knjf030hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf030hCtl = new knjf030hController;
var_dump($_REQUEST);
?>
