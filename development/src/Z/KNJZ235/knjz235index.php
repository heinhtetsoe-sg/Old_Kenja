<?php

require_once('for_php7.php');

require_once('knjz235Model.inc');
require_once('knjz235Query.inc');

class knjz235Controller extends Controller {
    var $ModelClassName = "knjz235Model";
    var $ProgramID      = "KNJZ235";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "knjz235":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjz235Model();       //コントロールマスタの呼び出し
                    $this->callView("knjz235Form1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("read");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "read";
                case "clear";
                    $this->callView("knjz235Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjz235Ctl = new knjz235Controller;
//var_dump($_REQUEST);
?>
