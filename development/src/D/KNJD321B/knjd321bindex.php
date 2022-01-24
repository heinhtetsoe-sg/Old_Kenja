<?php

require_once('for_php7.php');

require_once('knjd321bModel.inc');
require_once('knjd321bQuery.inc');

class knjd321bController extends Controller {
    var $ModelClassName = "knjd321bModel";
    var $ProgramID      = "KNJD321B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjd321b");
                    break 1;
                case "":
                case "knjd321b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd321bModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd321bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd321bCtl = new knjd321bController;
var_dump($_REQUEST);
?>
