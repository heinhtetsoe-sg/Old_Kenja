<?php

require_once('for_php7.php');

require_once('knjz220gModel.inc');
require_once('knjz220gQuery.inc');

class knjz220gController extends Controller {
    var $ModelClassName = "knjz220gModel";
    var $ProgramID      = "KNJZ220G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                case "knjz220g":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjz220gModel();       //コントロールマスタの呼び出し
                    $this->callView("knjz220gForm1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjz220g");
                    break 1;                
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjz220gCtl = new knjz220gController;
//var_dump($_REQUEST);
?>
