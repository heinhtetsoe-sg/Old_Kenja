<?php

require_once('for_php7.php');

require_once('knjf165Model.inc');
require_once('knjf165Query.inc');

class knjf165Controller extends Controller {
    var $ModelClassName = "knjf165Model";
    var $ProgramID      = "KNJF165";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf165":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf165Model();      //コントロールマスタの呼び出し
                    $this->callView("knjf165Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjf165");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf165Ctl = new knjf165Controller;
var_dump($_REQUEST);
?>
