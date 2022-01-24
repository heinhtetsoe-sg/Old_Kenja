<?php

require_once('for_php7.php');

require_once('knjf150eModel.inc');
require_once('knjf150eQuery.inc');

class knjf150eController extends Controller {
    var $ModelClassName = "knjf150eModel";
    var $ProgramID      = "KNJF150E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":  //更新
                    $sessionInstance->setAccessLogDetail("U", "KNJF150E");
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":  //削除
                    $sessionInstance->setAccessLogDetail("D", "KNJF150E");
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                case "sem":
                case "change":
                    $sessionInstance->knjf150eModel();     //コントロールマスタの呼び出し
                    $this->callView("knjf150eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf150eCtl = new knjf150eController;
?>
