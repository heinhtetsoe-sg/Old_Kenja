<?php
require_once('knjm829Model.inc');
require_once('knjm829Query.inc');

class knjm829Controller extends Controller {
    var $ModelClassName = "knjm829Model";
    var $ProgramID      = "KNJM829";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm829":                         //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjm829Model();   //コントロールマスタの呼び出し
                    $this->callView("knjm829Form1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm829Model();
                    $this->callView("knjm829Form1");
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
$knjm829Ctl = new knjm829Controller;
var_dump($_REQUEST);
?>
