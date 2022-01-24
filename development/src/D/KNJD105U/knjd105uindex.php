<?php

require_once('for_php7.php');

require_once('knjd105uModel.inc');
require_once('knjd105uQuery.inc');

class knjd105uController extends Controller {
    var $ModelClassName = "knjd105uModel";
    var $ProgramID      = "KNJD105U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105uHr":
                case "knjd105u":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd105uModel();
                    $this->callView("knjd105uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd105uCtl = new knjd105uController;
?>
