<?php

require_once('for_php7.php');

require_once('knjd425l_1Model.inc');
require_once('knjd425l_1Query.inc');

class knjd425l_1Controller extends Controller {
    var $ModelClassName = "knjd425l_1Model";
    var $ProgramID      = "KNJD425L_1";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "check":
                    //コントロールマスタの呼び出し
                    $sessionInstance->knjd425l_1Model();
                    $this->callView("knjd425l_1Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425l_2Form1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd425l_1Ctl = new knjd425l_1Controller;
?>
