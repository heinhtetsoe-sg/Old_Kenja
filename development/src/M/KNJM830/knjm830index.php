<?php
require_once('knjm830Model.inc');
require_once('knjm830Query.inc');

class knjm830Controller extends Controller {
    var $ModelClassName = "knjm830Model";
    var $ProgramID      = "KNJM830";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm830":                         //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjm830Model();   //コントロールマスタの呼び出し
                    $this->callView("knjm830Form1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm830Model();
                    $this->callView("knjm830Form1");
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
$knjm830Ctl = new knjm830Controller;
var_dump($_REQUEST);
?>
