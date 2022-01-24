<?php

require_once('for_php7.php');

require_once('knjm500wModel.inc');
require_once('knjm500wQuery.inc');

class knjm500wController extends Controller {
    var $ModelClassName = "knjm500wModel";
    var $ProgramID      = "KNJM500W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm500w":                         //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjm500wModel();   //コントロールマスタの呼び出し
                    $this->callView("knjm500wForm1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm500wModel();
                    $this->callView("knjm500wForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm500wCtl = new knjm500wController;
var_dump($_REQUEST);
?>
