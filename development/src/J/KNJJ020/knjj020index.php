<?php

require_once('for_php7.php');

require_once('knjj020Model.inc');
require_once('knjj020Query.inc');

class knjj020Controller extends Controller {
    var $ModelClassName = "knjj020Model";
    var $ProgramID      = "KNJJ020";

    function main() {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("grp_meb");
                    break 1;
                case "knjj020Form1":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjj020Model();       //コントロールマスタの呼び出し
                    $this->callView("knjj020Form1");
                    exit;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("grp_meb");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "grp_meb";
                case "clear";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj020Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjj020Ctl = new knjj020Controller;
//var_dump($_REQUEST);
?>
