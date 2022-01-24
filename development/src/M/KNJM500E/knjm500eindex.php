<?php

require_once('for_php7.php');

require_once('knjm500eModel.inc');
require_once('knjm500eQuery.inc');

class knjm500eController extends Controller {
    var $ModelClassName = "knjm500eModel";
    var $ProgramID      = "KNJM500E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm500e":                         //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjm500eModel();   //コントロールマスタの呼び出し
                    $this->callView("knjm500eForm1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm500eModel();
                    $this->callView("knjm500eForm1");
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
$knjm500eCtl = new knjm500eController;
var_dump($_REQUEST);
?>
